import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Server {

    List<Node> allServerNodes = new LinkedList<>();
    List<ServerSocketConnection> serverSocketConnectionList = new LinkedList<>();
    ServerSocket server;
    String Id;
    String port;
    String ipAddress;
    HashMap<String,ServerSocketConnection> serverSocketConnectionHashMap = new HashMap<>();
    PriorityQueue<RequestClient>  requestClientPriorityQueue = new PriorityQueue<>(new RequestComparator());
    Boolean locked = false;
    String lockForClient;
    Integer numberOfClients = 0;
    Integer currentReportCounter = 0;
    Integer releaseMessageCounter = 0;
    Integer grantMessageCounter = 0;
    Integer requestMessageCounter = 0;

    public Integer getNumberOfClients() {
        return numberOfClients;
    }

    public void setNumberOfClients(Integer numberOfClients) {
        this.numberOfClients = numberOfClients;
    }

    public List<Node> getAllServerNodes() {
        return allServerNodes;
    }

    public class CommandParser extends Thread{

        Server currentServer;

        public CommandParser(Server currentServer){
            this.currentServer = currentServer;
        }
        /*Command parser for server terminal */
        Pattern STATUS = Pattern.compile("^STATUS$");
        Pattern CLIENT_TEST = Pattern.compile("^CLIENT_TEST$");
        Pattern TEST_GRANT = Pattern.compile("^TEST_GRANT$");
        Pattern TRIGGER = Pattern.compile("^TRIGGER$");
        Pattern RESTART = Pattern.compile("^RESTART$");
        Pattern RESTART_TRIGGER = Pattern.compile("^RESTART_TRIGGER$");

        int rx_cmd(Scanner cmd){
            String cmd_in = null;
            if (cmd.hasNext())
                cmd_in = cmd.nextLine();
            Matcher m_STATUS = STATUS.matcher(cmd_in);
            Matcher m_CLIENT_TEST = CLIENT_TEST.matcher(cmd_in);
            Matcher m_TEST_GRANT = TEST_GRANT.matcher(cmd_in);
            Matcher m_TRIGGER = TRIGGER.matcher(cmd_in);
            Matcher m_RESTART = RESTART.matcher(cmd_in);
            Matcher m_RESTART_TRIGGER = RESTART_TRIGGER.matcher(cmd_in);

            if(m_STATUS.find()){
                System.out.println("SERVER SOCKET STATUS:");
                try {
                    System.out.println("STATUS UP");
                    System.out.println("SERVER ID: " + Id);
                    System.out.println("SERVER IP ADDRESS: " + ipAddress);
                    System.out.println("SERVER PORT: " + port);
                    System.out.println("NUMBER OF CLIENTS IN SYSTEM: " + numberOfClients);
                }
                catch (Exception e){
                    System.out.println("SOMETHING WENT WRONG IN TERMINAL COMMAND PROCESSOR");
                }
            }

            else if(m_CLIENT_TEST.find()){
                sendClientTest();
            }

            else if(m_TEST_GRANT.find()){
                sendClientGrantTest();
            }

            else if(m_TRIGGER.find()){
                sendTriggerToClient();
            }

            else if(m_RESTART.find()){
                clearAllClient();
            }

            else if(m_RESTART_TRIGGER.find()){
                sendRestartTriggerToClient();
            }


            return 1;
        }

        public void run() {
            System.out.println("Enter commands to set-up MESH Connection : TRIGGER");
            Scanner input = new Scanner(System.in);
            while(rx_cmd(input) != 0) { }
        }
    }


    /*Restart after deadlock functionality*/
    public void clearAllClient(){
        System.out.println("CLEAR ALL CLIENTS");
        Integer ClientId;
        for(ClientId=0; ClientId < serverSocketConnectionList.size() ; ClientId++){
            serverSocketConnectionHashMap.get(ClientId.toString()).sendRestart();
        }

    }

    /*Restart after deadlock functionality*/
    public void clearAllRunAgain(){
        System.out.println("SERVER RESET");
        requestClientPriorityQueue.clear();
        this.locked = false;
        this.currentReportCounter = 0;
        this.releaseMessageCounter = 0;
        this.grantMessageCounter = 0;
        this.requestMessageCounter = 0;

    }

    /*Functionality to trigger auto request in clients*/
    public synchronized void sendTriggerToClient(){
        System.out.println("Sending TRIGGER TO CLIENT");
        Integer ClientId;
        for(ClientId=0; ClientId < serverSocketConnectionList.size() ; ClientId++){
            serverSocketConnectionHashMap.get(ClientId.toString()).sendTrigger();
        }
    }

    /*Restart auto trigger after deadlock*/
    public synchronized void sendRestartTriggerToClient(){
        System.out.println("Sending TRIGGER TO CLIENT");
        Integer ClientId;
        for(ClientId=0; ClientId < serverSocketConnectionList.size() ; ClientId++){
            serverSocketConnectionHashMap.get(ClientId.toString()).sendRestartTrigger();
        }
    }

    /*Method to process request sent out from client*/
    public synchronized void processRequest(String requestingClientId, String requestTimeStamp){
        System.out.println("Inside process request for Client: " + requestingClientId + " with sequence number " + requestTimeStamp);
        if(!locked) {
            this.requestMessageCounter +=1;
            this.locked = true;
            this.lockForClient = requestingClientId;
            serverSocketConnectionHashMap.get(requestingClientId).sendGrant();
            this.grantMessageCounter +=1;
        }
        else if(locked){
            System.out.println("Server in locked state --- Adding to priority queue");
            this.requestMessageCounter +=1;
            requestClientPriorityQueue.add(new RequestClient(requestingClientId, Long.valueOf(requestTimeStamp)));
        }
    }

    /*Writing the stats sent out from client to file*/
    public synchronized void pushReportingClientMessage(String reportingClient, String reportingMessage){
        this.currentReportCounter += 1;
        try {
            System.out.println(reportingClient+" Client Completed 20 CS Execution Pending Completion " + this.currentReportCounter + " out of " + this.numberOfClients );
            BufferedWriter writer = new BufferedWriter(new FileWriter("stat.txt", true));
            writer.append(reportingClient+" Client -> "+reportingMessage+"\n");
            writer.close();
        }
        catch (Exception e){
            System.out.println("STATUS FILE WRITE ERROR");
        }
        if(this.currentReportCounter == this.getNumberOfClients()){
            System.out.println("++++++++++++++++++++++ ALL CLIENTS COMPLETED SIMULATION +++++++++++++++++++++++++++++");
            try {
                TimeUnit.SECONDS.sleep(3);
                serverSocketConnectionHashMap.get("0").pushServerStats();
            }
            catch(Exception e){
                System.out.println("Error while waiting to push server stats");
            }
        }
    }

    /*Writing server stats to terminal and writing to file*/
    public synchronized void logServerCounter(){
        System.out.println("LOG SERVER COUNTERS -------------------- END OF SIMULATION");
        String reportingMessage = "Request Message counter: " + this.requestMessageCounter + " Release Message Counter: " + this.releaseMessageCounter + " Grant Message Counter: " + this.grantMessageCounter;
        System.out.println(reportingMessage);
        try{
        BufferedWriter writer = new BufferedWriter(new FileWriter("stat.txt", true));
        writer.append(this.Id +" Server -> "+reportingMessage+"\n");
        writer.close();
        }
        catch(Exception e){
            System.out.println("Exception while pushing server stats: " + e);
        }

    }

    /*functionality to process the release message sent out from a client*/
    public synchronized void processRelease(String releasingClientId, String requestSequenceNumber){
        this.releaseMessageCounter +=1;
        System.out.println("Inside process RELEASE for Client: " + releasingClientId + " with sequence number " + requestSequenceNumber);
        if(this.lockForClient.equals(releasingClientId)) {
            if (requestClientPriorityQueue.isEmpty()) {
                System.out.println("The server is locked status for client " + this.lockForClient);
                this.locked = false;
            } else {
                System.out.println("The request queue was not empty");
                System.out.println("Sending grant to " + requestClientPriorityQueue.peek().clientId + " which had time stamp of " + requestClientPriorityQueue.peek().timeStamp);
                this.lockForClient = requestClientPriorityQueue.peek().clientId;
                System.out.println("SET lock to client: " + this.lockForClient);
                serverSocketConnectionHashMap.get(requestClientPriorityQueue.remove().clientId).sendGrant();
                this.grantMessageCounter +=1;

            }
        }

        else {
            System.out.println("$$$$ SERVER received release by Client it had not responded to");
        }
    }

    /*Send client test message*/
    public void sendClientTest(){
        Integer clientId;
        for (clientId = 0; clientId < this.serverSocketConnectionList.size(); clientId++){
            serverSocketConnectionList.get(clientId).clientTest();
        }
    }

    /*Test - sending grant message*/
    public void sendClientGrantTest(){
        Integer clientId;
        for (clientId = 0; clientId < this.serverSocketConnectionList.size(); clientId++){
            serverSocketConnectionList.get(clientId).sendGrant();
        }
    }

    /*reading server file and populating the list*/
    public void setServerList(){
        try {
            BufferedReader br = new BufferedReader(new FileReader("ServerAddressAndPorts.txt"));
            try {
                StringBuilder sb = new StringBuilder();
                String line = br.readLine();

                while (line != null) {
                    sb.append(line);
                    List<String> parsed_server = Arrays.asList(line.split(","));
                    Node n_server = new Node(parsed_server.get(0),parsed_server.get(1),parsed_server.get(2));
                    this.getAllServerNodes().add(n_server);
                    sb.append(System.lineSeparator());
                    line = br.readLine();
                }
                String everything = sb.toString();
                System.out.println(everything);
                System.out.println(this.getAllServerNodes().size());

            } finally {
                br.close();
            }
        }
        catch (Exception e) {
        }
    }



    /*Open a socket to list to connection request*/
    public void serverSocket(Integer serverId, Server currentServer){
        try
        {
            server = new ServerSocket(Integer.valueOf(this.allServerNodes.get(serverId).port));
            Id = Integer.toString(serverId);
            port  = this.allServerNodes.get(serverId).port;
            ipAddress = this.allServerNodes.get(serverId).ipAddress;
            System.out.println("Server node running on port " + Integer.valueOf(this.allServerNodes.get(serverId).port) +"," + " use ctrl-C to end");
            InetAddress myServerIp = InetAddress.getLocalHost();
            String ip = myServerIp.getHostAddress();
            String hostname = myServerIp.getHostName();
            System.out.println("Your current Server IP address : " + ip);
            System.out.println("Your current Server Hostname : " + hostname);
        }
        catch (IOException e)
        {
            System.out.println("Error creating socket");
            System.exit(-1);
        }

        Server.CommandParser cmdpsr = new Server.CommandParser(currentServer);
        cmdpsr.start();

        Thread current_node = new Thread() {
            public void run(){
                while(true){
                    try{
                        Socket s = server.accept();
                        ServerSocketConnection socketForServer = new ServerSocketConnection(s,Id, false,currentServer);
                        serverSocketConnectionList.add(socketForServer);
                        serverSocketConnectionHashMap.put(socketForServer.getRemote_id(),socketForServer);
                    }
                    catch(IOException e){ e.printStackTrace(); }
                }
            }
        };

        current_node.setDaemon(true);
        current_node.start();
    }



    public static void main(String[] args) {


        if (args.length != 1) {
            System.out.println("Usage: java Server <server-number>");
            System.exit(1);
        }

        System.out.println("Starting the Server");
        Server server = new Server();
        server.setServerList();// set server list
        server.serverSocket(Integer.valueOf(args[0]),server); // reserve a socket 
        System.out.println("Started the Server");
    }
}
