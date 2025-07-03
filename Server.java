/**
 *  MACHINE PROGRAM S13
 * 
 *  Borlaza, Clarence Bryant
 *  Campos, Annika Dominique
 *  Roman, Isaac Nathan <3
 */

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable {

    private static int port;
    private static ConcurrentHashMap<String, Boolean> registeredAliases = new ConcurrentHashMap<>();

    public Server(int port) {
        Server.port = port;
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server listening on port " + port);

            ExecutorService executor = Executors.newCachedThreadPool();

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getRemoteSocketAddress());

                executor.execute(new ClientHandler(clientSocket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class ClientHandler implements Runnable {
        private Socket clientSocket;
        private BufferedReader in;
        private PrintWriter out;
        private String clientNickname = "New User";

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;

            try {
                this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                this.out = new PrintWriter(clientSocket.getOutputStream(), true);
            } catch (Exception e) {
                System.out.println("Error in initializing streams ");
            }
        }

        @Override
        public void run() {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                out = new PrintWriter(clientSocket.getOutputStream(), true);

                String message;

                while ((message = in.readLine()) != null) {
                    System.out.println("Received from client: " + clientSocket.getRemoteSocketAddress() + ": " + message);

                    if (message.equals("/leave")) {
                        //add print indication that the client has left the server
                        disconnect();
                        break;
                    } else if (message.startsWith("/register")) { 
                        handleRegisterCommand(message);
                    } else if (message.equals("/?")) {
                        System.out.println("Client " + clientSocket.getRemoteSocketAddress() + " requested a list of commands");
                    } else if (message.equals("/dir")) {
                        sendDir();
                    } else if(message.startsWith("/get ")) {
                        String[] getCommandParts = message.split(" ");
                        String filename = getCommandParts[1];
                        File file = new File("serverdir/" + filename);

                        if (file.exists()) {
                            out.println("FILE_EXISTS");
                            out.flush();
                            out.println(filename);
                            out.flush();
                            fetchFile(message);
                            out.flush();
                        } else {
                            out.println("FILE_NOT_FOUND");
                            System.out.println("FILE NOT FOUND!");
                            out.flush();
                            out.println("Error: File not found in the server.");
                            out.flush();
                        }
                    } else if (message.startsWith("/store ")) {

                        if (in.readLine().contains("FILE_EXISTS")) {
                            String filename = in.readLine();
                            System.out.println("The file name is: " + filename);
                            receiveFile("/store " + filename);
                        } else if (in.readLine().equals("FILE_NOT_FOUND")) {
                            System.out.println(in.readLine());
                        }
                         
                    }
                }

                clientSocket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        
        private void handleRegisterCommand(String message) {
            String[] parts = message.split(" ");

            if (parts.length == 1){
                out.println("Invalid registration command. Please use: /register <alias>");
            }

            if (parts.length == 2) { 
                String alias = parts[1];
    
                if (checkAliasAvailability(alias)) {
                    clientNickname = alias;
                    sendMessage("Successfully registered with nickname: " + alias);
                    out.flush();
                    System.out.println("Client " + clientSocket.getRemoteSocketAddress() + " registered as: " + alias);
                } else {
                    sendMessage("Nickname already exists. Please choose a different one.");
                }
            } else {
                sendMessage("Invalid registration command. Please use: /register <alias>");
            }
        }
    
        private boolean checkAliasAvailability(String alias) {
            if (Server.registeredAliases.containsKey(alias)) {
                return false;
            } else {
                Server.registeredAliases.put(alias, true);
                return true;
            }
        }

        private void receiveFile(String command) {
            try {
                int bytes = 0;

                String[] getCommandParts = command.split(" ");
                String filename = getCommandParts[1];

                DataInputStream fileInput = new DataInputStream(clientSocket.getInputStream());
                FileOutputStream fileOutput = new FileOutputStream("serverdir/" + filename);

                long size = fileInput.readLong();
                byte[] buffer = new byte[4*1024];
                
                while (size > 0 && (bytes = fileInput.read(buffer, 0, (int)Math.min(buffer.length, size))) != -1) {
                    fileOutput.write(buffer, 0, bytes);
                    size -= bytes;
                }
                System.out.println("File saved to Server: " + filename);
                fileOutput.close();

            } catch (Exception e) {
                System.out.println("Error occurred in fetching the file from the server.");
            }
        }

        private void fetchFile(String command) {
            try {
                int bytes = 0;
                
                String[] getCommandParts = command.split(" ");
                String filename = getCommandParts[1];
                File file = new File("serverdir/" + filename);

                DataOutputStream fileWriter = new DataOutputStream(this.clientSocket.getOutputStream());
                FileInputStream fileReader = new FileInputStream(file);
                
                fileWriter.writeLong(file.length());
                byte[] buffer = new byte[4*1024];
                

                while ((bytes = fileReader.read(buffer)) != -1) {
                    fileWriter.write(buffer, 0, bytes);
                    fileWriter.flush();
                }
                fileReader.close();

        } catch (Exception e) {
            System.out.println("Error in file sending.");
        }
        }

        private void sendDir() {
            String currentDirectory = "serverdir";
            File directory = new File(currentDirectory);
            StringBuilder directoryInfo = new StringBuilder();

            File[] files = directory.listFiles();
            if (files != null && files.length > 0) {
                directoryInfo.append("Server Directory:\n");
                for (File file : files) {
                    directoryInfo.append(file.getName()).append("\n");
                }
            } else {
                directoryInfo.append("Server directory is empty.\n");
            }
            sendMessage(directoryInfo.toString());
        }

        private void sendMessage(String message) {
            out.println(message);
        }
        
        private void disconnect() {
            try {
                out.println("Connection closed. Thank you!");
                this.in.close();
                this.out.close();
                if (!this.clientSocket.isClosed()) {
                    this.clientSocket.close();
                }
            } catch (Exception e) {
                System.out.println("Error in disconnecting the client "); //insert nickname of client later
            }
        }
    }


    public static void main(String[] args) {
        Server server = new Server(12345);
        server.run();
    }
}
