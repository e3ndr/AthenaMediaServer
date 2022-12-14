package xyz.e3ndr.athena.server.http;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.rakurai.io.http.server.HttpServerImplementation;
import co.casterlabs.sora.Sora;
import co.casterlabs.sora.SoraFramework;
import co.casterlabs.sora.SoraLauncher;
import co.casterlabs.sora.api.SoraPlugin;
import lombok.NonNull;
import xyz.e3ndr.athena.Config;
import xyz.e3ndr.athena.server.AthenaServer;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;
import xyz.e3ndr.reflectionlib.ReflectionLib;

public class AthenaHttpServer implements AthenaServer {

    @Override
    public void start(Config config) {
        try {
            int port = config.getHttpPort();

            if (port == -1) return;

            SoraFramework framework = new SoraLauncher()
                .setPort(port)
                .setImplementation(HttpServerImplementation.UNDERTOW)
                .buildWithoutPluginLoader();

            framework
                .getSora()
                .register(new AthenaSoraAdapter());

            FastLogger serverLogger = ReflectionLib.getValue(framework.getServer(), "logger");
            serverLogger.setCurrentLevel(LogLevel.WARNING);

            framework.startHttpServer();

            FastLogger.logStatic("Started http server on %d!", port);
        } catch (Exception e) {
            FastLogger.logStatic(LogLevel.SEVERE, "Unable to start http server:\n%s", e);
        }
    }

    public static class AthenaSoraAdapter extends SoraPlugin {

        @Override
        public void onInit(Sora sora) {
            sora.addHttpProvider(this, new MediaRoutes());
            sora.addHttpProvider(this, new SessionRoutes());
            sora.addHttpProvider(this, new WiiMCRoutes());
            sora.addHttpProvider(this, new IngestApiRoutes());
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
            return "Athena";
        }

        @Override
        public @NonNull String getId() {
            return "athena";
        }

    }

}
