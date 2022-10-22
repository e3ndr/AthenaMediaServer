package xyz.e3ndr.athena.server.ftp;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import xyz.e3ndr.athena.Athena;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

/**
 * Code from https://github.com/pReya/ftpServer, adapted for the Athena project.
 * 
 * @author Moritz Stueckler
 */
public class Worker extends Thread implements Closeable {
    // Path information
    private String currDirectory;

    // control connection
    private Socket controlSocket;
    private PrintWriter controlOutWriter;
    private BufferedReader controlIn;

    // data Connection
    private int dataPort;
    private ServerSocket dataSocket;
    private Socket dataConnection;
    private PrintWriter dataOutWriter;

    // State
    private TransferType transferMode = TransferType.ASCII;
    private LoginState loginState = LoginState.WAITING_FOR_USERNAME;
    private String username;

    private boolean doControlLoop = true;

    private FastLogger logger;

    public Worker(Socket client, int dataPort) throws IOException {
        this.dataPort = dataPort;
        this.controlSocket = client;

        logger = new FastLogger(String.format("FTP Worker: %s", this.dataPort));

        try {
            this.controlIn = new BufferedReader(new InputStreamReader(this.controlSocket.getInputStream()));
            this.controlOutWriter = new PrintWriter(this.controlSocket.getOutputStream(), true);
        } catch (IOException e) {
            this.close();
        }
    }

    /* -------------------- */
    /* Processing           */
    /* -------------------- */

    @Override
    public void run() {
        try {
            // Greeting
            this.sendMessage(220, "Welcome to the Athena FTP-Server");

            // Get new command from client
            while (this.doControlLoop) {
                String line = this.controlIn.readLine();

                this.onCommand(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.close();
        }
    }

    private void onCommand(String line) {
        // split command and arguments
        int index = line.indexOf(' ');
        String command = ((index == -1) ? line.toUpperCase() : (line.substring(0, index)).toUpperCase());
        String args = ((index == -1) ? null : line.substring(index + 1));

        this.logger.debug("Command: %s, Args: %s", command, args);

        // dispatcher mechanism for different commands
        switch (command) {
            case "USER":
                command_USER(args);
                break;

            case "PASS":
                command_PASS(args);
                break;

            case "CWD":
                command_CWD(args);
                break;

            case "LIST":
                command_NLST(args);
                break;

            case "NLST":
                command_NLST(args);
                break;

            case "PWD":
            case "XPWD":
                command_PWD();
                break;

            case "QUIT":
                command_QUIT();
                break;

            case "PASV":
                command_PASV();
                break;

            case "EPSV":
                command_EPSV();
                break;

            case "SYST":
                command_SYST();
                break;

            case "FEAT":
                command_FEAT();
                break;

            case "PORT":
                command_PORT(args);
                break;

            case "EPRT":
                command_EPORT(args);
                break;

            case "RETR":
                command_RETR(args);
                break;

            case "MKD":
            case "XMKD":
                command_MKD(args);
                break;

            case "RMD":
            case "XRMD":
                command_RMD(args);
                break;

            case "TYPE":
                command_TYPE(args);
                break;

            case "STOR":
                command_STOR(args);
                break;

            default:
                this.sendMessage(501, "Unknown command");
                break;

        }
    }

    /* -------------------- */
    /* Connection & Lifecycle */
    /* -------------------- */

    @Override
    public void close() {
        try {
            this.controlIn.close();
            this.controlOutWriter.close();
            this.controlSocket.close();

            this.closeDataConnection();
        } catch (IOException ignored) {} finally {
            AthenaFtpServer.openPorts.add(this.dataPort);
        }
    }

    private void closeDataConnection() {
        if (this.dataConnection != null) {
            try {
                this.dataOutWriter.close();
                this.dataConnection.close();
                if (this.dataSocket != null) {
                    this.dataSocket.close();
                }

                this.logger.debug("Closed Data Connection.");
            } catch (IOException ignored) {}

            this.dataOutWriter = null;
            this.dataConnection = null;
            this.dataSocket = null;
        }
    }

    private void sendMessage(int statusCode, String format, Object... args) {
        String message = String.format(format, args);
        controlOutWriter.printf("%d %s", statusCode, message);
    }

    private void sendLineMessage(int statusCode, String... messages) {
        for (int idx = 0; idx < messages.length; idx++) {
            String message = messages[idx];
            boolean isLast = idx == messages.length - 1;

            if (isLast) {
                controlOutWriter.printf("%d %s", statusCode, message);
            } else {
                controlOutWriter.printf("%d-%s", statusCode, message);
            }
        }
    }

    private void sendDataMsgToClient(String msg) {
        if (this.dataConnection == null || this.dataConnection.isClosed()) {
            this.sendMessage(425, "No data connection was established");
            this.logger.warn("Cannot send message, because no data connection is established");
        } else {
            this.dataOutWriter.print(msg + '\r' + '\n');
        }
    }

    private void openDataConnectionPassive(int port) {
        try {
            this.dataSocket = new ServerSocket(port);
            this.dataConnection = this.dataSocket.accept();
            this.dataOutWriter = new PrintWriter(this.dataConnection.getOutputStream(), true);
            this.logger.debug("Established Passive mode connection!");
        } catch (IOException e) {
            e.printStackTrace();
            this.logger.severe("Unable to establish Passive Mode connection:\n%s", e);
            this.close();
        }
    }

    private void openDataConnectionActive(String ipAddress, int port) {
        try {
            dataConnection = new Socket(ipAddress, port);
            dataOutWriter = new PrintWriter(this.dataConnection.getOutputStream(), true);
            this.logger.debug("Established Active mode connection!");
        } catch (IOException e) {
            this.logger.severe("Unable to establish Active Mode connection:\n%s", e);
        }

    }

    /* -------------------- */
    /* Authentication Commands */
    /* -------------------- */

    /**
     * Handler for USER command. User identifies the client.
     * 
     * @param username Username entered by the user
     */
    private void command_USER(String username) {
        switch (this.loginState) {
            case LOGGED_IN:
                this.sendMessage(530, "User already logged in");
                break;

            case WAITING_FOR_USERNAME:
                this.username = username;
                loginState = LoginState.ENTERED_USERNAME;
                this.sendMessage(331, "User name okay, need password");
                break;

            default:
                break;
        }
    }

    /**
     * Handler for PASS command. PASS receives the user password and checks if it's
     * valid.
     * 
     * @param password Password entered by the user
     */
    private void command_PASS(String password) {
        switch (this.loginState) {
            case ENTERED_USERNAME:
                if (Athena.authenticate(password)) {
                    this.loginState = LoginState.LOGGED_IN;
                    this.sendLineMessage(
                        230,
                        "Welcome to Athena", "User logged in successfully"
                    );
                } else {
                    this.sendMessage(530, "Not logged in");
                }
                break;

            case LOGGED_IN:
                this.sendMessage(530, "User already logged in");
                break;

            default:
                break;
        }
    }

    /* -------------------- */
    /* Connection Commands  */
    /* -------------------- */

    /**
     * Handler for PASV command which initiates the passive mode. In passive mode
     * the client initiates the data connection to the server. In active mode the
     * server initiates the data connection to the client.
     */
    private void command_PASV() {
        // Using fixed IP for connections on the same machine
        // For usage on separate hosts, we'd need to get the local IP address from
        // somewhere
        // Java sockets did not offer a good method for this
        String myIp = "127.0.0.1";
        String myIpSplit[] = myIp.split("\\.");

        int p1 = this.dataPort / 256;
        int p2 = this.dataPort % 256;

        this.sendMessage(
            227,
            "Entering Passive Mode (%s,%s,%s,%s,%d,%d)",
            myIpSplit[0], myIpSplit[1], myIpSplit[2], myIpSplit[3],
            p1, p2
        );
        this.openDataConnectionPassive(this.dataPort);
    }

    /**
     * Handler for EPSV command which initiates extended passive mode. Similar to
     * PASV but for newer clients (IPv6 support is possible but not implemented
     * here).
     */
    private void command_EPSV() {
        this.sendMessage(229, "Entering Extended Passive Mode (|||%s|)", this.dataPort);
        openDataConnectionPassive(this.dataPort);
    }

    /**
     * Handler for the QUIT command.
     */
    private void command_QUIT() {
        this.sendMessage(221, "Closing connection");
        this.doControlLoop = false;
    }

    /**
     * Handler for the PORT command. The client issues a PORT command to the server
     * in active mode, so the server can open a data connection to the client
     * through the given address and port number.
     * 
     * @param args The first four segments (separated by comma) are the IP address.
     *             The last two segments encode the port number (port = seg1*256 +
     *             seg2)
     */
    private void command_PORT(String args) {
        // Extract IP address and port number from arguments
        String[] stringSplit = args.split(",");
        String hostName = stringSplit[0] + "." + stringSplit[1] + "." + stringSplit[2] + "." + stringSplit[3];

        int p = Integer.parseInt(stringSplit[4]) * 256 + Integer.parseInt(stringSplit[5]);

        // Initiate data connection to client
        this.openDataConnectionActive(hostName, p);
        sendMessage(200, "Command OK");
    }

    /**
     * Handler for the EPORT command. The client issues an EPORT command to the
     * server in active mode, so the server can open a data connection to the client
     * through the given address and port number.
     * 
     * @param args This string is separated by vertical bars and encodes the IP
     *             version, the IP address and the port number
     */
    private void command_EPORT(String args) {
        final String IPV4 = "1";
        final String IPV6 = "2";

        // Example arg: |2|::1|58770| or |1|132.235.1.2|6275|
        String[] splitArgs = args.split("\\|");
        String ipVersion = splitArgs[1];
        String ipAddress = splitArgs[2];

        if (!IPV4.equals(ipVersion) || !IPV6.equals(ipVersion)) {
            throw new IllegalArgumentException("Unsupported IP version");
        }

        int port = Integer.parseInt(splitArgs[3]);

        // Initiate data connection to client
        this.openDataConnectionActive(ipAddress, port);
        this.sendMessage(200, "Command OK");
    }

    /**
     * Handler for the TYPE command. The type command sets the transfer mode to
     * either binary or ascii mode
     * 
     * @param mode Transfer mode: "a" for Ascii. "i" for Image (Binary).
     */
    private void command_TYPE(String mode) {
        switch (mode.toLowerCase()) {
            case "A": {
                this.transferMode = TransferType.ASCII;
                this.sendMessage(200, "OK");
                break;
            }

            case "I": {
                this.transferMode = TransferType.BINARY;
                this.sendMessage(200, "OK");
                break;
            }

            default: {
                this.sendMessage(504, "Not OK");
                break;
            }
        }
    }

    /* -------------------- */
    /* Directory Commands   */
    /* -------------------- */

    /**
     * Handler for NLST (Named List) command. Lists the directory content in a short
     * format (names only)
     * 
     * @param args The directory to be listed
     */
    private void command_NLST(String args) {
//        if (dataConnection == null || dataConnection.isClosed()) {
//            sendMessage("425 No data connection was established");
//        } else {
//
//            String[] dirContent = nlstHelper(args);
//
//            if (dirContent == null) {
//                sendMessage("550 File does not exist.");
//            } else {
//                sendMessage("125 Opening ASCII mode data connection for file list.");
//
//                for (int i = 0; i < dirContent.length; i++) {
//                    sendDataMsgToClient(dirContent[i]);
//                }
//
//                sendMessage("226 Transfer complete.");
//                closeDataConnection();
//
//            }
//        }
    }

    /**
     * Handler for CWD (change working directory) command.
     * 
     * @param args New directory to be created
     */
    private void command_CWD(String args) {
//        String filename = currDirectory;
//
//        // go one level up (cd ..)
//        if (args.equals("..")) {
//            int ind = filename.lastIndexOf(fileSeparator);
//            if (ind > 0) {
//                filename = filename.substring(0, ind);
//            }
//        }
//
//        // if argument is anything else (cd . does nothing)
//        else if ((args != null) && (!args.equals("."))) {
//            filename = filename + fileSeparator + args;
//        }
//
//        // check if file exists, is directory and is not above root directory
//        File f = new File(filename);
//
//        if (f.exists() && f.isDirectory() && (filename.length() >= root.length())) {
//            currDirectory = filename;
//            sendMessage("250 The current directory has been changed to " + currDirectory);
//        } else {
//            sendMessage("550 Requested action not taken. File unavailable.");
//        }
    }

    /**
     * Handler for PWD (Print working directory) command. Returns the path of the
     * current directory back to the client.
     */
    private void command_PWD() {
//        this.sendMessage("257 \"" + currDirectory + "\"");
    }

    /**
     * Handler for the MKD (make directory) command. Creates a new directory on the
     * server.
     * 
     * @param args Directory name
     */
    private void command_MKD(String args) {
//        // Allow only alphanumeric characters
//        if (args != null && args.matches("^[a-zA-Z0-9]+$")) {
//            File dir = new File(currDirectory + fileSeparator + args);
//
//            if (!dir.mkdir()) {
//                sendMessage("550 Failed to create new directory");
//                debugOutput("Failed to create new directory");
//            } else {
//                sendMessage("250 Directory successfully created");
//            }
//        } else {
//            sendMessage("550 Invalid name");
//        }
    }

    /**
     * Handler for RMD (remove directory) command. Removes a directory.
     * 
     * @param dir directory to be deleted.
     */
    private void command_RMD(String dir) {
//        String filename = currDirectory;
//
//        // only alphanumeric folder names are allowed
//        if (dir != null && dir.matches("^[a-zA-Z0-9]+$")) {
//            filename = filename + fileSeparator + dir;
//
//            // check if file exists, is directory
//            File d = new File(filename);
//
//            if (d.exists() && d.isDirectory()) {
//                d.delete();
//
//                sendMessage("250 Directory was successfully removed");
//            } else {
//                sendMessage("550 Requested action not taken. File unavailable.");
//            }
//        } else {
//            sendMessage("550 Invalid file name.");
//        }
    }

    /**
     * Handler for the RETR (retrieve) command. Retrieve transfers a file from the
     * ftp server to the client.
     * 
     * @param file The file to transfer to the user
     */
    private void command_RETR(String file) {
//        File f = new File(currDirectory + fileSeparator + file);
//
//        if (!f.exists()) {
//            sendMessage("550 File does not exist");
//        }
//
//        else {
//
//            // Binary mode
//            if (transferMode == TransferType.BINARY) {
//                BufferedOutputStream fout = null;
//                BufferedInputStream fin = null;
//
//                sendMessage("150 Opening binary mode data connection for requested file " + f.getName());
//
//                try {
//                    // create streams
//                    fout = new BufferedOutputStream(dataConnection.getOutputStream());
//                    fin = new BufferedInputStream(new FileInputStream(f));
//                } catch (Exception e) {
//                    debugOutput("Could not create file streams");
//                }
//
//                debugOutput("Starting file transmission of " + f.getName());
//
//                // write file with buffer
//                byte[] buf = new byte[1024];
//                int l = 0;
//                try {
//                    while ((l = fin.read(buf, 0, 1024)) != -1) {
//                        fout.write(buf, 0, l);
//                    }
//                } catch (IOException e) {
//                    debugOutput("Could not read from or write to file streams");
//                    e.printStackTrace();
//                }
//
//                // close streams
//                try {
//                    fin.close();
//                    fout.close();
//                } catch (IOException e) {
//                    debugOutput("Could not close file streams");
//                    e.printStackTrace();
//                }
//
//                debugOutput("Completed file transmission of " + f.getName());
//
//                sendMessage("226 File transfer successful. Closing data connection.");
//
//            }
//
//            // ASCII mode
//            else {
//                sendMessage("150 Opening ASCII mode data connection for requested file " + f.getName());
//
//                BufferedReader rin = null;
//                PrintWriter rout = null;
//
//                try {
//                    rin = new BufferedReader(new FileReader(f));
//                    rout = new PrintWriter(dataConnection.getOutputStream(), true);
//
//                } catch (IOException e) {
//                    debugOutput("Could not create file streams");
//                }
//
//                String s;
//
//                try {
//                    while ((s = rin.readLine()) != null) {
//                        rout.println(s);
//                    }
//                } catch (IOException e) {
//                    debugOutput("Could not read from or write to file streams");
//                    e.printStackTrace();
//                }
//
//                try {
//                    rout.close();
//                    rin.close();
//                } catch (IOException e) {
//                    debugOutput("Could not close file streams");
//                    e.printStackTrace();
//                }
//                sendMessage("226 File transfer successful. Closing data connection.");
//            }
//
//        }
//        closeDataConnection();
    }

    /**
     * Handler for STOR (Store) command. Store receives a file from the client and
     * saves it to the ftp server.
     * 
     * @param file The file that the user wants to store on the server
     */
    private void command_STOR(String file) {
//        if (file == null) {
//            sendMessage("501 No filename given");
//        } else {
//            File f = new File(currDirectory + fileSeparator + file);
//
//            if (f.exists()) {
//                sendMessage("550 File already exists");
//            }
//
//            else {
//
//                // Binary mode
//                if (transferMode == TransferType.BINARY) {
//                    BufferedOutputStream fout = null;
//                    BufferedInputStream fin = null;
//
//                    sendMessage("150 Opening binary mode data connection for requested file " + f.getName());
//
//                    try {
//                        // create streams
//                        fout = new BufferedOutputStream(new FileOutputStream(f));
//                        fin = new BufferedInputStream(dataConnection.getInputStream());
//                    } catch (Exception e) {
//                        debugOutput("Could not create file streams");
//                    }
//
//                    debugOutput("Start receiving file " + f.getName());
//
//                    // write file with buffer
//                    byte[] buf = new byte[1024];
//                    int l = 0;
//                    try {
//                        while ((l = fin.read(buf, 0, 1024)) != -1) {
//                            fout.write(buf, 0, l);
//                        }
//                    } catch (IOException e) {
//                        debugOutput("Could not read from or write to file streams");
//                        e.printStackTrace();
//                    }
//
//                    // close streams
//                    try {
//                        fin.close();
//                        fout.close();
//                    } catch (IOException e) {
//                        debugOutput("Could not close file streams");
//                        e.printStackTrace();
//                    }
//
//                    debugOutput("Completed receiving file " + f.getName());
//
//                    sendMessage("226 File transfer successful. Closing data connection.");
//
//                }
//
//                // ASCII mode
//                else {
//                    sendMessage("150 Opening ASCII mode data connection for requested file " + f.getName());
//
//                    BufferedReader rin = null;
//                    PrintWriter rout = null;
//
//                    try {
//                        rin = new BufferedReader(new InputStreamReader(dataConnection.getInputStream()));
//                        rout = new PrintWriter(new FileOutputStream(f), true);
//
//                    } catch (IOException e) {
//                        debugOutput("Could not create file streams");
//                    }
//
//                    String s;
//
//                    try {
//                        while ((s = rin.readLine()) != null) {
//                            rout.println(s);
//                        }
//                    } catch (IOException e) {
//                        debugOutput("Could not read from or write to file streams");
//                        e.printStackTrace();
//                    }
//
//                    try {
//                        rout.close();
//                        rin.close();
//                    } catch (IOException e) {
//                        debugOutput("Could not close file streams");
//                        e.printStackTrace();
//                    }
//                    sendMessage("226 File transfer successful. Closing data connection.");
//                }
//
//            }
//            closeDataConnection();
//        }
    }

    /* -------------------- */
    /* Feature/About Commands */
    /* -------------------- */

    private void command_SYST() {
        this.sendMessage(215, "COMP4621 FTP Server Homebrew");
    }

    /**
     * Handler for the FEAT (features) command. Feat transmits the
     * abilities/features of the server to the client. Needed for some ftp clients.
     * This is just a dummy message to satisfy clients, no real feature information
     * included.
     */
    private void command_FEAT() {
        this.sendLineMessage(211, "Extensions supported:", "END"); // Dummy.
    }

}
