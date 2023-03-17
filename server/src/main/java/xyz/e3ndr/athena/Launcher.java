package xyz.e3ndr.athena;

import java.io.File;
import java.nio.file.Files;

import co.casterlabs.commons.async.AsyncTask;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.serialization.JsonParseException;
import xyz.e3ndr.athena.server.ftp.AthenaFtpServer;
import xyz.e3ndr.athena.server.http.AthenaHttpServer;
import xyz.e3ndr.fastloggingframework.FastLoggingFramework;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

public class Launcher {
    private static final File configFile = new File("config.json");

    private static final FastLogger logger = new FastLogger();

    public static void main(String[] args) throws Exception {
        ClassLoader.getPlatformClassLoader().setDefaultAssertionStatus(true);

        Config config = null;

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

                // omdbApiKey
                Athena.omdbApiKey = config.getOmdbApiKey();

                logger.debug("Using config: %s", config);
            } catch (JsonParseException e) {
                logger.severe("Unable to parse config file, is it malformed?\n%s", e);
            }
        } else {
            config = new Config();
            logger.info("Config file doesn't exist, creating a new file.");
        }

        Files.writeString(configFile.toPath(), Rson.DEFAULT.toJson(config).toString(true));

        Config $config_pointer = config;
        AsyncTask.createNonDaemon(() -> new AthenaHttpServer().start($config_pointer));
        AsyncTask.createNonDaemon(() -> new AthenaFtpServer().start($config_pointer));
    }

}
