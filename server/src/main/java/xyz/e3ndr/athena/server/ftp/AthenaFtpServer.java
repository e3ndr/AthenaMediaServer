package xyz.e3ndr.athena.server.ftp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import xyz.e3ndr.athena.Config;
import xyz.e3ndr.athena.server.AthenaServer;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

/**
 * Code from https://github.com/pReya/ftpServer, adapted for the Athena project.
 * 
 * @author Moritz Stueckler
 */
public class AthenaFtpServer implements AthenaServer {
    private static final int MAX_CLIENTS = 100;
    static final List<Integer> openPorts = Collections.synchronizedList(new LinkedList<>());

    @SuppressWarnings("resource")
    @Override
    public void start(Config config) {
        int controlPort = config.getFtpPort();
        if (controlPort == -1) return;

        // Generate a list of ports.
        for (int port = controlPort + 1; port < controlPort + MAX_CLIENTS; port++) {
            openPorts.add(port);
        }

        try (ServerSocket welcomeListener = new ServerSocket(controlPort)) {
            FastLogger.logStatic("Started ftp server on %d!", controlPort);

            while (true) {
                try {
                    Socket client = welcomeListener.accept();
                    int dataPort = openPorts.remove(0); // Passive mode port.

                    // Create new worker thread for new connection
                    new FtpClient(client, dataPort)
                        .start();
                } catch (IOException e) {
                    FastLogger.logStatic(LogLevel.SEVERE, "Unable to accept new connection:\n%s", e);
                }
            }
        } catch (IOException e) {
            FastLogger.logStatic(LogLevel.SEVERE, "Unable to start ftp server:\n%s", e);
        }
    }

}
