I have attached 15 files in the elearning. They are,
1. Client.java
2. ClientAddressAndPorts.txt
3. Message.java
4. Node.java
5. Server.java
6. ServerAddressAndPorts.txt
7. ServerSocketConnection.java
8. SocketConnection.java
9. WriteRequest.java
10. RequestClient.java
11. RequestComparator.java
12. cs_log.txt
13. stat.txt
14. QuorumConfig.txt
15. README

_______________________________________________________

I have used a windows machine to run my project on the dcxy machines.

Using the UTD VPN, I have connected to dc01,dc02,dc03,dc04,dc05,dc06,dc07,dc08 using putty. Putty is a software used to make SSH connection to the machines.

Install the Putty Software and open eight windows of this software. In the first one type netid@dc01.utdallas.edu and in the other one type netid@dc02.utdallas.edu and so on till dc08. After that, Click on open to establish connections to those machines.

NOTE: Use only dc01,dc02,dc03,dc04,dc05,dc06,dc07,dc08 to get the desired output.

______________________________________________________

In the dc01.utdallas.edu do the following commands.

Copy all the attached files using touch and nano commands.Since it is a replicate server, all files are cloned in the server and client machines.

1.In dc01,
#javac *.java - To compile all the files.

2. Start all server with command
    java Server <serverId>
3. Start all Clients with command
    java Client <clientId> <delay between request in ms>
4. use TRIGGER command in Server 0 terminal to start auto request.
5. In case of deadlock - User server 0 terminal
    RESTART
    RESTART TRIGGER
6. To Change the delay between requests in client. Using client terminal.
    SET_ELAPSE <Change delay in ms>
7. Once all the simulation are run - In server 0 terminal you'll see
    * ALL SIMULATIONS COMPLETED *













