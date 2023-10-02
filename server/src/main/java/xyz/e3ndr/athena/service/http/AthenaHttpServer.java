package xyz.e3ndr.athena.service.http;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.sora.Sora;
import co.casterlabs.sora.SoraFramework;
import co.casterlabs.sora.SoraLauncher;
import co.casterlabs.sora.api.SoraPlugin;
import lombok.NonNull;
import xyz.e3ndr.athena.Athena;
import xyz.e3ndr.athena.service.AthenaService;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

public class AthenaHttpServer implements AthenaService {

    @Override
    public void init() {
        try {
            int port = Athena.config.services.http.port;

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

    static class AthenaSoraAdapter extends SoraPlugin {

        @Override
        public void onInit(Sora sora) {
            sora.addProvider(this, new MetaRoutes());
            sora.addProvider(this, new MediaRoutes());
            sora.addProvider(this, new StreamRoutes());
            sora.addProvider(this, new SessionRoutes());
            sora.addProvider(this, new IngestApiRoutes());
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