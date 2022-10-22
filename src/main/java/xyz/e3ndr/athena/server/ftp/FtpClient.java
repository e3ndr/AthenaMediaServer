package xyz.e3ndr.athena.server.ftp;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

import xyz.e3ndr.athena.Athena;
import xyz.e3ndr.athena.types.AudioCodec;
import xyz.e3ndr.athena.types.ContainerFormat;
import xyz.e3ndr.athena.types.VideoCodec;
import xyz.e3ndr.athena.types.VideoQuality;
import xyz.e3ndr.athena.types.media.Media;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

/**
 * Code from https://github.com/pReya/ftpServer, adapted for the Athena project.
 * 
 * @author Moritz Stueckler
 */
class FtpClient extends Thread implements Closeable {

    // quality information, parsed from username
    private ContainerFormat containerFormat = ContainerFormat.MP4;
    private VideoQuality videoQuality = VideoQuality.SOURCE;
    private VideoCodec videoCodec = VideoCodec.H264;
    private AudioCodec audioCodec = AudioCodec.SOURCE;

    // control connection
    private Socket controlSocket;
    private PrintWriter controlOutWriter;
    private BufferedReader controlIn;

    // data connection
    private int dataPort;
    private ServerSocket dataSocket;
    private Socket dataConnection;
    private PrintWriter dataOutWriter;

    // state
    private TransferType transferMode = TransferType.ASCII;
    private LoginState loginState = LoginState.WAITING_FOR_USERNAME;
    private String username;

    private boolean doControlLoop = true;

    private FastLogger logger;

    public FtpClient(Socket client, int dataPort) throws IOException {
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
        this.logger.debug("Connection opened.");

        try {
            // Greeting
            this.sendMessage(220, "Welcome to the Athena FTP-Server");

            // Get new command from client
            while (this.doControlLoop) {
                String line = this.controlIn.readLine();

                if (line == null) {
                    this.doControlLoop = false;
                    break;
                }

                this.onCommand(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.close();
        }
    }

    private void onCommand(String line) {
        this.logger.debug("\u2193 %s", line);

        // split command and arguments
        int index = line.indexOf(' ');
        String command = ((index == -1) ? line.toUpperCase() : (line.substring(0, index)).toUpperCase());
        String args = ((index == -1) ? null : line.substring(index + 1));

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
                command_LIST(args);
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
            this.logger.debug("Connection closed.");
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

    private void writeControl(String line) {
        this.controlOutWriter.println(line);
        this.logger.debug("\u2191 %s", line);
    }

    private void sendMessage(int statusCode, String format, Object... args) {
        String message = String.format(format, args);
        this.writeControl(String.format("%d %s", statusCode, message));
    }

    private void sendMultilineMessage(int statusCode, String... messages) {
        for (int idx = 0; idx < messages.length; idx++) {
            String message = messages[idx];
            boolean isLast = idx == messages.length - 1;

            if (isLast) {
                this.writeControl(String.format("%d %s", statusCode, message));
            } else {
                this.writeControl(String.format("%d-%s", statusCode, message));
            }
        }
    }

    private void sendDataMsgToClient(String msg) {
        if (this.dataConnection == null || this.dataConnection.isClosed()) {
            this.sendMessage(425, "No data connection was established");
            this.logger.warn("Cannot send message because there is no data connection active.");
        } else {
            this.dataOutWriter.print(msg + '\r' + '\n');
        }
    }

    private void openDataConnectionPassive(int port) {
        try {
            this.dataSocket = new ServerSocket(port);
            this.dataConnection = this.dataSocket.accept();
            this.dataOutWriter = new PrintWriter(this.dataConnection.getOutputStream(), true);
            this.logger.debug("Established Passive Mode connection!");
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
            this.logger.debug("Established Active Mode connection!");
        } catch (IOException e) {
            this.logger.severe("Unable to establish Active Mode connection:\n%s", e);
        }

    }

    private void onAuthenticated() {
        // example: format_mp4.quality_hd.videocodec_h264.audiocodec_aac
        String[] parts = this.username.toLowerCase().split("\\.");

        for (String part : parts) {
            String[] splitPart = part.split("_", 2);
            if (splitPart.length != 2) continue;

            String key = splitPart[0];
            String value = splitPart[1].toUpperCase();

            switch (key) {
                case "format": {
                    this.containerFormat = ContainerFormat.valueOf(value);
                    break;
                }

                case "quality": {
                    this.videoQuality = VideoQuality.valueOf(value);
                    break;
                }

                case "videocodec": {
                    this.videoCodec = VideoCodec.valueOf(value);
                    break;
                }

                case "audiocodec": {
                    this.audioCodec = AudioCodec.valueOf(value);
                    break;
                }
            }
        }

        this.sendMultilineMessage(
            230,
            "Welcome to Athena",
            String.format(
                "Chosen Parameters: format=%s, quality=%s, videoCodec=%s, audioCodec=%s",
                this.containerFormat, this.videoQuality, this.videoCodec, this.audioCodec
            ),
            "User logged in successfully"
        );
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
                    this.onAuthenticated();
                    this.loginState = LoginState.LOGGED_IN;
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

        if (!IPV4.equals(ipVersion) && !IPV6.equals(ipVersion)) {
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
     * @param mode Transfer mode: "A" for Ascii. "I" for Image (Binary).
     */
    private void command_TYPE(String mode) {
        switch (mode.toUpperCase()) {
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
     * Handler for the RETR (retrieve) command. Retrieve transfers a file from the
     * ftp server to the client.
     * 
     * @param file The file to transfer to the user
     */
    private void command_RETR(String file) {
        // TODO
        this.sendMessage(501, "Unknown command");
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
     * Handler for NLST (Named List) command. Lists the directory content in a short
     * format (names only)
     * 
     * @param args The directory to be listed
     */
    private void command_NLST(String args) {
        if (dataConnection == null || dataConnection.isClosed()) {
            this.sendMessage(425, "No data connection was established");
        } else {
            List<String> files = new LinkedList<>();

            for (Media media : Athena.listMedia()) {
                files.add(
                    String.format(
                        "%s.%s",
                        media.toString(), this.containerFormat.name().toLowerCase()
                    )
                );
            }

            this.sendMessage(125, "Opening ASCII mode data connection for file list.");

            for (String file : files) {
                this.sendDataMsgToClient(file);
            }

            this.sendMessage(226, "Transfer complete.");
            this.closeDataConnection();
        }
    }

    /**
     * Handler for LIST command. Lists the directory content in /bin/ls format.
     * 
     * @param args The directory to be listed
     */
    private void command_LIST(String args) {
        if (dataConnection == null || dataConnection.isClosed()) {
            this.sendMessage(425, "No data connection was established");
        } else {
            List<String> files = new LinkedList<>();

            for (Media media : Athena.listMedia()) {
                int day = media.getInfo().getDay();
                if (day <= 0) day = 1;

                int month = media.getInfo().getMonth();
                String monthStr = monthToString(month);
                if (monthStr == null) monthStr = "Jan";

                int year = media.getInfo().getYear();
                if (year <= 0) year = 0;

                String ls = String.format(
                    "-rw-r--r-- 1 Athena Media          1337 %s %02d %04d %s [%s].%s",
                    monthStr, day, year,
                    media.getInfo().getTitle(), media.getId(), this.containerFormat.name().toLowerCase()
                );

                files.add(ls);
                this.logger.trace("LIST Entry: %s", ls);
            }

            this.sendMessage(125, "Opening ASCII mode data connection for file list.");

            for (String file : files) {
                this.sendDataMsgToClient(file);
            }

            this.sendMessage(226, "Transfer complete.");
            this.closeDataConnection();
        }
    }

    /**
     * Handler for CWD (change working directory) command.
     * 
     * @param args New directory to be created
     */
    private void command_CWD(String args) {
        this.sendMessage(550, "Requested action not taken");
    }

    /**
     * Handler for PWD (Print working directory) command. Returns the path of the
     * current directory back to the client.
     */
    private void command_PWD() {
        this.sendMessage(257, "\"/\"");
    }

    /**
     * Handler for the MKD (make directory) command. Creates a new directory on the
     * server.
     * 
     * @param args Directory name
     */
    private void command_MKD(String args) {
        this.sendMessage(550, "Requested action not taken");
    }

    /**
     * Handler for RMD (remove directory) command. Removes a directory.
     * 
     * @param dir directory to be deleted.
     */
    private void command_RMD(String dir) {
        this.sendMessage(550, "Requested action not taken");
    }

    /**
     * Handler for STOR (Store) command. Store receives a file from the client and
     * saves it to the ftp server.
     * 
     * @param file The file that the user wants to store on the server
     */
    private void command_STOR(String file) {
        this.sendMessage(552, "Exceeded storage allocation");
    }

    /* -------------------- */
    /* Feature/About Commands */
    /* -------------------- */

    private void command_SYST() {
        this.sendMessage(215, "AthenaMediaServer FTP Server Homebrew");
    }

    /**
     * Handler for the FEAT (features) command. Feat transmits the
     * abilities/features of the server to the client. Needed for some ftp clients.
     * This is just a dummy message to satisfy clients, no real feature information
     * included.
     */
    private void command_FEAT() {
        this.sendMultilineMessage(211, "Extensions supported:", "END"); // Dummy.
    }

    /* -------------------- */
    /* Helpers              */
    /* -------------------- */

    private String monthToString(int month) {
        switch (month) {
            case 1:
                return "Jan";
            case 2:
                return "Feb";
            case 3:
                return "Mar";
            case 4:
                return "Apr";
            case 5:
                return "May";
            case 6:
                return "Jun";
            case 7:
                return "Jul";
            case 8:
                return "Aug";
            case 9:
                return "Sep";
            case 10:
                return "Oct";
            case 11:
                return "Nov";
            case 12:
                return "Dec";
            default:
                return null;
        }
    }

}
