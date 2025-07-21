Java File Exchange System
=========================
A client-server file exchange application built in Java, enabling users to register aliases and transfer files to and from a central server.

--------------------------------------------------------------------------------

🌟 PROJECT OVERVIEW
------------------

This project is a command-line file exchange system implemented in Java using TCP sockets. It consists of a multi-threaded server and a client application that communicate over a network. The system allows multiple clients to connect simultaneously, register a unique alias, and perform file operations such as listing server files, uploading files to the server ('/store'), and downloading files from the server ('/get'). The server manages client connections and file storage, while the client provides a user-friendly command interface to interact with the system.


🎯 MOTIVATION
------------

The primary motivation for this project was to apply and demonstrate core networking and concurrent programming concepts in Java. Key goals included:

  * Network Programming: To implement a client-server architecture using Java's 'java.net' package ('Socket', 'ServerSocket').
  * Concurrency: To build a multi-threaded server using 'ExecutorService' to handle multiple client connections simultaneously without blocking.
  * File I/O: To manage file transfers efficiently using Java's input and output streams ('FileInputStream', 'FileOutputStream', 'DataInputStream', 'DataOutputStream').
  * Command-Line Interface (CLI): To create an interactive and protocol-driven CLI for both the client and server applications.
  * State Management: To manage shared server state, such as registered user aliases, in a thread-safe manner using 'ConcurrentHashMap'.


🛠️ TECHNOLOGIES USED
-------------------

  * Java: The core programming language for both the client and server.
  * Java Networking (java.net): 'Socket' and 'ServerSocket' for TCP communication.
  * Java I/O Streams: For handling console input and file transfers.
  * Java Concurrency (java.util.concurrent): 'ExecutorService' and 'ConcurrentHashMap' for multi-threading and thread-safe data structures.


🚀 GETTING STARTED
-----------------

To compile and run this project, you will need a Java Development Kit (JDK) installed.

### Prerequisites

  * Java Development Kit (JDK) 8 or higher.

### Project Structure

The system is composed of two main Java classes:

  * 'Server.java': The server application that listens for client connections, manages registered aliases, and handles file storage in a 'serverdir/' directory.
  * 'Client.java': The client application that connects to the server and provides users with a command-line interface to perform actions.

Additionally, the following directories must be created:
  * 'serverdir/': In the same directory as the server application, to store files uploaded by clients.
  * 'clientdir/': In the same directory as the client application, to store files that the client will upload.
  * 'receiveddir/': In the same directory as the client application, to save files downloaded from the server.

### Compilation

1.  Open your terminal or command prompt.
2.  Navigate to the directory containing the '.java' source files.
3.  Compile both the Server and Client source files:

        javac Server.java Client.java


🤝 USAGE
-------

The Server must be running before clients can connect.

1.  Start the Server:
    In a terminal window, run the compiled Server class:

        java Server

    The server will start listening for connections on the default port (12345).

2.  Start the Client:
    In a separate terminal window, run the compiled Client class:

        java Client

3.  Interact with the Client CLI:
    The client will start and await your commands. The primary commands are:

    *   Connect to the server: '/join <server_ip> <port>'
        Example: '/join 127.0.0.1 12345'
    *   Register an alias: '/register <alias>'
        Example: '/register Alice'
        (Note: You must register before using other commands.)
    *   List files on the server: '/dir'
    *   Upload a file to the server: '/store <filename>'
        (The file must exist in your 'clientdir/' folder.)
        Example: '/store 1.txt'
    *   Download a file from the server: '/get <filename>'
        (The file will be saved to your 'receiveddir/' folder.)
        Example: '/get 2.txt'
    *   Disconnect from the server: '/leave'
    *   Display command help: '/?'
    *   Exit the client application: '/exit'
