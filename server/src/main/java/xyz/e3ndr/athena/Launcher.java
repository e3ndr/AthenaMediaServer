package xyz.e3ndr.athena;

import java.io.File;
import java.nio.file.Files;

import co.casterlabs.commons.async.AsyncTask;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.serialization.JsonParseException;
import co.casterlabs.sora.SoraFramework;
import xyz.e3ndr.athena.service.ftp.AthenaFtpServer;
import xyz.e3ndr.athena.service.http.AthenaHttpServer;
import xyz.e3ndr.athena.service.simple_ui.AthenaSimpleUIServer;
import xyz.e3ndr.fastloggingframework.FastLoggingFramework;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

public class Launcher {
    private static final File configFile = new File("config.json");

    private static final FastLogger logger = new FastLogger();

    public static void main(String[] args) throws Exception {
        ClassLoader.getPlatformClassLoader().setDefaultAssertionStatus(true);

        // Some pre-init.
        SoraFramework.LOGGER.setCurrentLevel(LogLevel.WARNING);

        // Load the config.
        if (configFile.exists()) {
            try {
                Athena.config = Rson.DEFAULT.fromJson(Files.readString(configFile.toPath()), Config.class);

                logger.debug("Using config: %s", Athena.config);
            } catch (JsonParseException e) {
                logger.severe("Unable to parse config file, is it malformed?\n%s", e);
            }
        } else {
            logger.info("Config file doesn't exist, creating a new file.");
        }

        Files.writeString(configFile.toPath(), Rson.DEFAULT.toJson(Athena.config).toString(true));

        // disableColoredConsole
        FastLoggingFramework.setColorEnabled(!Athena.config.console.disableColor);

        // debug
        FastLoggingFramework.setDefaultLevel(Athena.config.console.debug ? LogLevel.DEBUG : LogLevel.INFO);

        // Go!
        if (Athena.config.services.http.enable) {
            AsyncTask.createNonDaemon(() -> new AthenaHttpServer().init());
        }

        if (Athena.config.services.http.enable) {
            AsyncTask.createNonDaemon(() -> new AthenaFtpServer().init());
        }

        if (Athena.config.services.simpleUI.enable) {
            AsyncTask.createNonDaemon(() -> new AthenaSimpleUIServer().init());
        }
    }

}
