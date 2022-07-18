import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Date;

/*Client socket connection handler class. This class is used to consume the connection buffer to send out message and redirect to functionality for received messages*/
public class SocketConnection {

    Socket otherClient;
    String my_id;
    String remote_id;
    BufferedReader in;
    PrintWriter out;
    Boolean Initiator;
    Client my_master;
    String numberOfClients;

    public String getRemote_id() {
        return remote_id;
    }

    public void setRemote_id(String remote_id) {
        this.remote_id = remote_id;
    }

    public SocketConnection(Socket otherClient, String myId, Client my_master, String numberOfClients) {
        this.otherClient = otherClient;
        this.my_id = myId;
        this.my_master = my_master;
        this.numberOfClients = numberOfClients;
        try{
            in = new BufferedReader(new InputStreamReader(this.otherClient.getInputStream()));
            out = new PrintWriter(this.otherClient.getOutputStream(), true);
        }
        catch (Exception e){
            System.out.println("Exception while creating buffer in out for socket connection");
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

            if(cmd_in.equals("SEND_CLIENT_ID")){
                out.println(this.my_id);
                out.println(this.numberOfClients);
            }

            else if(cmd_in.equals("CLIENT_TEST")){
                System.out.println("CLIENT TEST RECEIVED");
            }

            else if(cmd_in.equals("GRANT")){
                String serverSendingGrant = cmd.readLine();
                System.out.println("GRANT received from server " + serverSendingGrant);
                my_master.processGrant(serverSendingGrant);
            }

            else if(cmd_in.equals("PUSH_SERVER_STATS")){
                my_master.pushServerStats();
            }

            else if(cmd_in.equals("TRIGGER")){
                System.out.println("RECEIVED TRIGGER FROM SERVER ");
                my_master.autoRequest();
            }

            else if(cmd_in.equals("RESTART_CLIENT")){
                System.out.println("RESET CLIENT RECEIVED");
                my_master.clearClient();
            }

            else if(cmd_in.equals("RESTART_TRIGGER")){
                System.out.println("**************** CLIENT RESTART TRIGGER RECEIVED ************************************");
                my_master.processRestartTrigger();
            }

        }
        catch (Exception e){}
        return 1;
    }


    public synchronized  void serverTest() {
        out.println("SERVER_TEST");
    }

    public synchronized void serverRequestTest(){
        out.println("REQUEST");
        out.println(this.my_id);
        out.println("0");
    }

    public synchronized void serverReleaseTest(){
        out.println("RELEASE");
        out.println(this.my_id);
        out.println("0");
    }

    public synchronized void sendRequest(){
        out.println("REQUEST");
        out.println(this.my_id);
        Date date = new Date();
        out.println(date.getTime());
    }

    public synchronized void sendRelease(){
        out.println("RELEASE");
        out.println(this.my_id);
        out.println("0");
    }


    public synchronized void sendStats(String note){
        out.println("CLIENT_STATS");
        out.println(my_id);
        out.println(note);
    }

    public synchronized void pushServerStats(){
        out.println("SERVER_STATS");
    }

    public synchronized void sendServerRestart(){
        out.println("RESTART_SERVER");
    }
    public Socket getOtherClient() {
        return otherClient;
    }

    public void setOtherClient(Socket otherClient) {
        this.otherClient = otherClient;
    }
}
