/**
 *  MACHINE PROGRAM S13
 * 
 *  Borlaza, Clarence Bryant
 *  Campos, Annika Dominique
 *  Roman, Isaac Nathan <3
 */

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client implements Runnable {
    
    private Boolean isClientConnectedtoServer;
    private Boolean isRegistered;
    private Boolean exit;
    private BufferedReader in;
    private PrintWriter out;
    private Socket socket;
    private String serverAddress;
    private int port;

    public Client(String serverAddress, int port) {
        this.serverAddress = serverAddress;
        this.port = port;
        this.exit = false;
        this.isClientConnectedtoServer = false;
        this.isRegistered = false;
    }

    @Override
    public void run() {
        try {
            Scanner s = new Scanner(System.in);
            String input;

            while (!isClientConnectedtoServer && !exit) {
                System.out.print("Input: ");
                input = s.nextLine();

                if (input.startsWith("/join ")) {
                    String[] parts = input.split(" ");
                    if (parts.length == 3) {
                        serverAddress = parts[1];
                        try {
                            port = Integer.parseInt(parts[2]);

                            try {
                                this.socket = new Socket(serverAddress, port);
                                isClientConnectedtoServer = true;
                                System.out.println("Connected to the server at " + serverAddress + ":" + port);

                                this.out = new PrintWriter(this.socket.getOutputStream(), true);
                                this.in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));

                                while (isClientConnectedtoServer && !exit) {
                                    System.out.print("Input: ");
                                    input = s.nextLine();

                                    // Input: /register Alice
                                    if (input.equals("/leave")){ //disconnect to the server
                                        out.println(input);
                                        disconnect();
                                    } else if(input.equals("/?")) {
                                        out.println(input);
                                        displaySyntaxCommands();
                                    } else if (input.startsWith("/register ")) {
                                        out.println(input);
                                        isRegistered = true;

                                        String serverResponse = in.readLine();
                                        System.out.println(serverResponse);

                                    } else if (isRegistered == false) {
                                        System.out.println("Please register your handle first to access the commands. Use /? for more information.");
                                    } else if (input.equals("/dir")) { // request dir from server
                                        out.println(input);
                                        String response;
                                        while ((response = in.readLine()) != null && !response.isEmpty()) {
                                            System.out.println(response);
                                        }
                                    } else if(input.startsWith("/get ")) {
                                        String[] getCommandParts = input.split(" ");
                                        if (getCommandParts.length == 2) {
                                            out.println(input);
                                            if (in.readLine().equals("FILE_EXISTS")) {
                                                String filename = in.readLine();
                                                System.out.println("The file name is: " + filename);
                                                getFile(filename);
                                            } else {
                                                System.out.println("File not found in this server.");
                                            }
                                        } else {
                                            System.out.println("Error: Command parameters do not match or is not allowed.");
                                        }
                                    } else if(input.startsWith("/store ")){
                                        String[] getCommandParts = input.split(" ");
                                        String filename = getCommandParts[1];
                                        File file = new File("clientdir/" + filename);
                
                                        if (file.exists()) {
                                            out.println("/store ");
                                            out.println("FILE_EXISTS");
                                            out.flush();
                                            out.println(filename);
                                            out.flush();
                                            sendFile(input);
                                            out.flush();
                                        } else {
                                            out.println("FILE_NOT_FOUND");
                                            System.out.println("FILE NOT FOUND!");
                                            out.flush();
                                            out.println("Error: File not found in the server.");
                                            out.flush();
                                        }
                                    
                                    } else {
                                        System.out.println("Error: Unknown command. Use: /? to see the list of all commands.");
                                    }
                                }
                                
                            } catch (Exception e) {
                                System.out.println("Failed to connect to the server at " + serverAddress + ":" + port);
                            }
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid port number. Please enter a valid port.");
                        }
                    } else {
                        System.out.println("Invalid command. Use: /join <server_ip_add> <port>");
                    }
                } else if(input.equals("/?")) {
                    displaySyntaxCommands();
                } else if (input.equals("/exit")) { //exit program
                    exit = true;
                    System.out.println("Terminating application...");
                } else {
                    System.out.println("Invalid command. Use: /join <server_ip_add> <port>, /?, or /exit.");
                }
            }

            s.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendFile(String command) {

        try {

            int bytes = 0;
            
            String[] getCommandParts = command.split(" ");
            String filename = getCommandParts[1];
            File file = new File("clientdir/"+ filename);
            
            DataOutputStream fileWriter = new DataOutputStream(socket.getOutputStream());
            FileInputStream fileReader = new FileInputStream("clientdir/" + filename);
 
            fileWriter.writeLong(file.length());
            byte[] buffer = new byte[4*1024];
            

            while ((bytes = fileReader.read(buffer)) != -1) {
                fileWriter.write(buffer, 0, bytes);
                fileWriter.flush();
            }
            fileReader.close();

            System.out.println("File " + filename + " sent to server.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getFile(String filename) {
        try {
            int bytes = 0;

            DataInputStream fileInput = new DataInputStream(this.socket.getInputStream());
            FileOutputStream fileOutput = new FileOutputStream("receiveddir/" + filename);

            long size = fileInput.readLong();
            byte[] buffer = new byte[4*1024];

            while (size > 0 && (bytes = fileInput.read(buffer, 0, (int)Math.min(buffer.length, size))) != -1) {
                fileOutput.write(buffer, 0, bytes);
                size -= bytes;
            }
            System.out.println("File received from Server: " + filename);
            fileOutput.close();
        } catch (Exception e) {
            System.out.println("Error occurred in fetching the file from the server.");
        }
    }
    
    private void disconnect() {
        this.isClientConnectedtoServer = false;
        this.exit = true;
        try {
            String leave = in.readLine();
            System.out.println(leave);
            this.out.close();
            this.in.close();
            if(!this.socket.isClosed()) {
                this.socket.close();
            }
        } catch (Exception e) {
            System.out.println("Error: Disconnection failed. Please connect to the server first.");
        }
    }

    public void displaySyntaxCommands() {
        System.out.println("Input Syntax Commands: ");
        System.out.println("-----------------------------------------------------------------------------------");
        System.out.println("1. Connection to the server application         | /join <server_ip_add> <port>");
        System.out.println("2. Disconnect to the server applciation         | /leave");
        System.out.println("3. Register a unique handle or alias            | /register <handle>");
        System.out.println("4. Send file to server                          | /store <filename>");
        System.out.println("5. Request directory file list from a server    | /dir");
        System.out.println("6. Fetch a file from a server                   | /get <filename>");
        System.out.println("7. Request command help to output all commands  | /?\n   Syntax commands for references");
        System.out.println("-----------------------------------------------------------------------------------");
        System.out.println();
    }
    public static void main(String[] args) {
        Client client = new Client("127.0.0.1", 12345);
        client.run();
    }
}