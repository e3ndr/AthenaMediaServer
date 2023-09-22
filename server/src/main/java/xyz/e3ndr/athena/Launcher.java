package xyz.e3ndr.athena;

import java.io.File;
import java.nio.file.Files;

import co.casterlabs.commons.async.AsyncTask;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.serialization.JsonParseException;
import co.casterlabs.sora.SoraFramework;
import lombok.Getter;
import xyz.e3ndr.athena.server.ftp.AthenaFtpServer;
import xyz.e3ndr.athena.server.http.AthenaHttpServer;
import xyz.e3ndr.athena.webui.AthenaUIServer;
import xyz.e3ndr.fastloggingframework.FastLoggingFramework;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

public class Launcher {
    private static final File configFile = new File("config.json");

    private static final FastLogger logger = new FastLogger();

    private static @Getter Config config;

    public static void main(String[] args) throws Exception {
        ClassLoader.getPlatformClassLoader().setDefaultAssertionStatus(true);

        // Some pre-init.
        SoraFramework.LOGGER.setCurrentLevel(LogLevel.WARNING);

        // Load the config.
        if (configFile.exists()) {
            try {
                config = Rson.DEFAULT.fromJson(Files.readString(configFile.toPath()), Config.class);

                // disableColoredConsole
                FastLoggingFramework.setColorEnabled(!config.isDisableColoredConsole());

                // debug
                FastLoggingFramework.setDefaultLevel(config.isDebug() ? LogLevel.DEBUG : LogLevel.INFO);

                // enableCudaAcceleration
                Athena.enableCudaAcceleration = config.isEnableCudaAcceleration();

                // mediaDirectory
                Athena.mediaDirectory = config.getMediaDirectory();
                if (!Athena.mediaDirectory.exists()) {
                    logger.info("Media directory doesn't exist, creating it now.");
                    Athena.mediaDirectory.mkdirs();
                } else if (!Athena.mediaDirectory.isDirectory()) {
                    logger.fatal("Media directory is not actually a directory, crashing.");
                    System.exit(-1);
                }

                // cacheDirectory
                Athena.cacheDirectory = config.getCacheDirectory();
                if (!Athena.cacheDirectory.exists()) {
                    logger.info("Cache directory doesn't exist, creating it now.");
                    Athena.cacheDirectory.mkdirs();
                } else if (!Athena.cacheDirectory.isDirectory()) {
                    logger.fatal("Cache directory is not actually a directory, crashing.");
                    System.exit(-1);
                }

                // ingestDirectory
                Athena.ingestDirectory = config.getIngestDirectory();
                if (!Athena.ingestDirectory.exists()) {
                    logger.info("Ingest directory doesn't exist, creating it now.");
                    Athena.ingestDirectory.mkdirs();
                } else if (!Athena.ingestDirectory.isDirectory()) {
                    logger.fatal("Ingest directory is not actually a directory, crashing.");
                    System.exit(-1);
                }

                logger.debug("Using config: %s", config);
            } catch (JsonParseException e) {
                logger.severe("Unable to parse config file, is it malformed?\n%s", e);
            }
        } else {
            config = new Config();
            logger.info("Config file doesn't exist, creating a new file.");
        }

        Files.writeString(configFile.toPath(), Rson.DEFAULT.toJson(config).toString(true));

        // Go!
        AsyncTask.createNonDaemon(() -> new AthenaHttpServer().start(config));
        AsyncTask.createNonDaemon(() -> new AthenaFtpServer().start(config));
        AsyncTask.createNonDaemon(() -> new AthenaUIServer().start(config));
    }

}
