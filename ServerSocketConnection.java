import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;


/*Server socket connection handler class. This class is used to consume the connection buffer to send out message and redirect to functionality for received messages*/
public class ServerSocketConnection {
    Socket otherClient;
    String my_id;
    String remote_id;
    BufferedReader in;
    PrintWriter out;
    Boolean Initiator;
    Server my_master;
    String numberOfClients;

    public String getRemote_id() {
        return remote_id;
    }

    public void setRemote_id(String remote_id) {
        this.remote_id = remote_id;
    }

    public ServerSocketConnection(Socket otherClient, String myId, Boolean isServer, Server my_master) {
        this.otherClient = otherClient;
        this.my_id = myId;
        this.my_master = my_master;
        try{
            in = new BufferedReader(new InputStreamReader(this.otherClient.getInputStream()));
            out = new PrintWriter(this.otherClient.getOutputStream(), true);
        }
        catch (Exception e){

        }

        try {
            if(!isServer) {
                out.println("SEND_CLIENT_ID");
                System.out.println("SEND_CLIENT_ID request sent");
                remote_id = in.readLine();
                numberOfClients = in .readLine();
                if(my_master.getNumberOfClients() == 0){
                    my_master.setNumberOfClients(Integer.valueOf(numberOfClients));
                }
                System.out.println("SEND_CLIENT_ID request response received with ID: " + remote_id);
            }
        }

        catch (Exception e){

        }
        Thread read = new Thread(){
            public void run(){
                while(rx_cmd(in,out) != 0) { }
            }
        };
        read.setDaemon(true); 	// terminate when main ends
        read.start();
    }


    public int rx_cmd(BufferedReader cmd,PrintWriter out) {
        try {
            String cmd_in = cmd.readLine();
            if (cmd_in.equals("SERVER_TEST")) {
                System.out.println("Test write received from sender");
            }

            else if(cmd_in.equals("REQUEST")) {
                String requestingClientId = cmd.readLine();
                String requestSequenceNumber = cmd.readLine();
                System.out.println("Received REQUEST from client " + requestingClientId + "which had the sequence number: " + requestSequenceNumber );
                my_master.processRequest(requestingClientId,requestSequenceNumber);
            }

            else if(cmd_in.equals("RELEASE")){
                String releaseClientId = cmd.readLine();
                String releaseSequenceNumber = cmd.readLine();
                System.out.println("Received RELEASE from client " + releaseClientId + "which had the sequence number: " + releaseSequenceNumber );
                my_master.processRelease(releaseClientId,releaseSequenceNumber);
            }

            else if(cmd_in.equals("CLIENT_STATS")){
                String reportingClientId = cmd.readLine();
                String reportingClientMessage = cmd.readLine();
                my_master.pushReportingClientMessage(reportingClientId,reportingClientMessage);
            }

            else if(cmd_in.equals("SERVER_STATS")){
                System.out.println("Server received push server stats");
                my_master.logServerCounter();
            }

            else if(cmd_in.equals("RESTART_SERVER")){
                my_master.clearAllRunAgain();
            }
        }
        catch (Exception e){}
        return 1;
    }


    public synchronized void clientTest(){
        out.println("CLIENT_TEST");
    }

    public synchronized void sendGrant(){
        out.println("GRANT");
        out.println(my_id);
    }

    public synchronized void pushServerStats(){
        System.out.println("SENDING PUSH SERVER STATS TO CLIENT 0");
        out.println("PUSH_SERVER_STATS");
    }

    public synchronized void sendRestart(){
        System.out.println("SENDING RESTART TO ALL CLIENT ");
        out.println("RESTART_CLIENT");
    }

    public synchronized void sendTrigger(){
        out.println("TRIGGER");
    }

    public synchronized void sendRestartTrigger(){
        out.println("RESTART_TRIGGER");
    }

    public Socket getOtherClient() {
        return otherClient;
    }

    public void setOtherClient(Socket otherClient) {
        this.otherClient = otherClient;
    }
}
