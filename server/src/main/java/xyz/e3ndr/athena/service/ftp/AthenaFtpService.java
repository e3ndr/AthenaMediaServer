package xyz.e3ndr.athena.service.ftp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import xyz.e3ndr.athena.Athena;
import xyz.e3ndr.athena.service.AthenaService;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

/**
 * Code from https://github.com/pReya/ftpServer, adapted for the Athena project.
 * 
 * @author Moritz Stueckler
 */
public class AthenaFtpService implements AthenaService {
    private static final int MAX_CLIENTS = 100;
    private static final int FTP_PORT_RANGE_OFFSET = 100;
    static final List<Integer> openPorts = Collections.synchronizedList(new LinkedList<>());

    @SuppressWarnings("resource")
    @Override
    public void init() {
        int controlPort = Athena.config.services.ftp.port;

        // Generate a list of ports.
        for (int idx = 0; idx < +MAX_CLIENTS; idx++) {
            openPorts.add(idx + controlPort + FTP_PORT_RANGE_OFFSET);
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
