import java.net.*;
import java.io.*;
import java.util.Scanner;
import java.util.ArrayList;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;

public class TextEditor {

    //class variables
    String sentence = null;
    Socket clientSocket = null;
    Socket connectionSocket = null;
    DataOutputStream out = null;
    DataInputStream in = null;
    String cmd = "";
    boolean myFlag = false;

    static int numberOfClients = 0;

    //constructors
    TextEditor(){};
    TextEditor(Socket s){
        this.connectionSocket = s;
        numberOfClients++;
    }

    //client main method
    public static void main(String[] args) throws Exception {
        //establish connection
        TextEditor client = new TextEditor();
        client.clientMethod(args);

    }

    //client logic
    private void clientMethod(String[] args) throws Exception{
        try{
            String server = args[0];
            int portNumber = Integer.parseInt(args[1]);
            String receivedMsg;


            clientSocket = new Socket(server, portNumber);
            out = new DataOutputStream(clientSocket.getOutputStream());
            in = new DataInputStream(clientSocket.getInputStream());
            System.out.println("~ Connection OK");



            if(authentication()){
                do{
                    if (myFlag) break;
                    //main dish
                    Scanner scan = new Scanner(System.in);
                    cmd = scan.nextLine();

                    //UPDT Logic
                    if((cmd.split(" ",2)[0]).equals("UPDT")){

                        sendToServer(cmd + "\r\n");
                        receivedMsg = getFromServer();

                        if (receivedMsg.split(" ", 2)[0].equals("INVALID")) {
                            System.out.println("~" + receivedMsg.split(" ", 3)[1] + " is already the last version.");
                        } else if (receivedMsg.split(" ", 2)[0].equals("OK")) {
                            System.out.println("~ The current version is " +
                                    (receivedMsg.split(" ", 3)[1]) + ".");
                        } else {
                            System.out.println("~ Error: something went wrong. Please try again.");
                        }

                    } else

                    //WRTE Logic
                    if((cmd.split(" ",2)[0]).equals("WRTE")){

                        sendToServer(cmd + "\r\n");
                        receivedMsg = getFromServer();

                        if (receivedMsg.split(" ", 2)[0].equals("INVALID")) {
                            if (receivedMsg.split(" ", 3)[1].equals("No")) {
                                System.out.println("~ Error: no such line exists.");
                            } else {
                                System.out.println("~ Error: the current version is " +
                                        receivedMsg.split(" ", 3)[1] + ", please get an update.");
                            }
                        } else if (receivedMsg.split(" ", 2)[0].equals("OK")) {
                            System.out.println("~ Write successful. Please UPDT to apply the change.");
                        } else {
                            System.out.println("~ Error: something went wrong. Please try again.");
                        }

                    } else

                    //APND Logic
                    if((cmd.split(" ",2)[0]).equals("APND")){

                        sendToServer(cmd + "\r\n");
                        receivedMsg = getFromServer();

                        if (receivedMsg.split(" ", 2)[0].equals("INVALID")) {
                            if (!isAnInt(cmd.split(" ", 3)[1])) {
                                System.out.println("~ Error: wrong syntax.");
                            } else {
                                System.out.println("~ Error: the current version is " +
                                        receivedMsg.split(" ", 3)[1] + ", please get an update.");
                            }
                        } else if (receivedMsg.split(" ", 2)[0].equals("OK")) {
                            System.out.println("~ Append successful.");
                        } else {
                            System.out.println("~ Error: something went wrong. Please try again.");
                        }

                    } else if (!(cmd.split(" ",2)[0]).equals("EXIT"))
                        System.out.println("~ Error: Command not recognized.");

                } while(!cmd.equals("EXIT"));

                System.out.println("~ Thanks for using the TextEditor. Bye!");
            }
        }
        catch(IOException ioexception){
            ioexception.printStackTrace();
        }
        finally{
            try{
                //in.close();
                out.close();
				clientSocket.close();
				//scan.close();
            }
            catch(IOException ioexception){
                ioexception.printStackTrace();
            }
        }
    }

    //function that checks authentication
    private boolean authentication()throws IOException{
        boolean authenticationCheck = false;

        while(!authenticationCheck){
            String messageByServer = "";
            Scanner scan = new Scanner(System.in);

            cmd = scan.nextLine();


            while(!((cmd.split(" ",2)[0]).equals("USER"))) {
                if (cmd.split(" ", 2)[0].equals("EXIT")) {
                    myFlag = true;
                    return true;
                } else if ( (cmd.split(" ",2)[0]).equals("APND") ||
                       (cmd.split(" ",2)[0]).equals("WRTE") ||
                        (cmd.split(" ",2)[0]).equals("UPDT") )
                    System.out.println("~ Authentication required! Enter user name.");
                else if ( (cmd.split(" ",2)[0]).equals("PASS") )
                    System.out.println("~ Enter user name first!");
                else
                    System.out.println("~ Error: command not recognized. Authentication required to proceed.");

                cmd = scan.nextLine();
                //System.out.println(cmd);
            }
            sendToServer(cmd+"\r\n");

            //wait until server replies
            messageByServer = getFromServer();

            while(!((cmd.split(" ",2)[0]).equals("PASS"))) {
                System.out.println("~ Password required!");
                scan = new Scanner(System.in);
                cmd = scan.nextLine();
            }
            sendToServer(cmd+"\r\n");

            //see if authentication confirmed or not
            messageByServer = getFromServer();

            if ((messageByServer.split(" ",2)[0]).equals("OK")){
                scan = new Scanner(System.in);
                authenticationCheck = true;
            }
            else{
                System.out.println("~ Authentication failed!");
                authenticationCheck = false;
            }


            if(authenticationCheck){
                System.out.println("~ Authentication done.");
            }
        }
        return authenticationCheck;
    }

    //logic to send commands to Server
    private void sendToServer(String command) throws IOException{
        try{
            BufferedReader buffReader = new BufferedReader(new InputStreamReader(System.in));
            clientSocket.getOutputStream().write(command.getBytes());
        }
        catch(IOException ioexc){
            ioexc.printStackTrace();
        }
    }

    //logic to receive message from Server
    private String getFromServer() throws IOException {
        byte[] messageFromServer = new byte[1024];
        clientSocket.getInputStream().read(messageFromServer);
        String message = new String(messageFromServer);
        return message;
    }


    private boolean isAnInt(String s) {
        return s.matches("-?\\d+");
    }

}