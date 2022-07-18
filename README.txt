_______________________________________________________

I have used a windows machine to run my project on eight machines say 01, 02, 03, 04, 05, 06, 07, 08.
______________________________________________________

In the machine-01 do the following commands.

Copy all the attached files using touch and nano commands.Since it is a replicate server, all files are cloned in the server and client machines.

1.In machine-01,
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













