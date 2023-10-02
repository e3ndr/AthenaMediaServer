package xyz.e3ndr.athena.webui;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.sora.Sora;
import co.casterlabs.sora.SoraFramework;
import co.casterlabs.sora.SoraLauncher;
import co.casterlabs.sora.api.SoraPlugin;
import lombok.NonNull;
import xyz.e3ndr.athena.Athena;
import xyz.e3ndr.athena.server.AthenaServer;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

public class AthenaUIServer implements AthenaServer {

    @Override
    public void start() {
        try {
            int port = Athena.config.services.simpleUI.port;

            SoraFramework framework = new SoraLauncher()
                .setPort(port)
                .buildWithoutPluginLoader();

            framework
                .getSora()
                .register(new AthenaSoraAdapter());

            framework
                .getServer()
                .getLogger()
                .setCurrentLevel(LogLevel.WARNING);

            framework.startHttpServer();

            FastLogger.logStatic("Started http server on %d!", port);
        } catch (Exception e) {
            FastLogger.logStatic(LogLevel.SEVERE, "Unable to start http server:\n%s", e);
        }
    }

    public static class AthenaSoraAdapter extends SoraPlugin {

        @Override
        public void onInit(Sora sora) {
            sora.addProvider(this, new UIRoutes());
            sora.addProvider(this, new InternalRoutes());
        }

        @Override
        public void onClose() {}

        @Override
        public @Nullable String getVersion() {
            return null;
        }

        @Override
        public @Nullable String getAuthor() {
            return null;
        }

        @Override
        public @NonNull String getName() {
            return "Athena Web UI";
        }

        @Override
        public @NonNull String getId() {
            return "athena-webui";
        }

    }

}
