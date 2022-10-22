package xyz.e3ndr.athena.server.http;

import java.io.IOException;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.rakurai.io.http.server.HttpServerImplementation;
import co.casterlabs.sora.Sora;
import co.casterlabs.sora.SoraFramework;
import co.casterlabs.sora.SoraLauncher;
import co.casterlabs.sora.api.SoraPlugin;
import lombok.NonNull;
import xyz.e3ndr.athena.Config;
import xyz.e3ndr.athena.server.AthenaServer;

public class AthenaHttpServer implements AthenaServer {

    @Override
    public void start(Config config) throws IOException {
        int port = config.getHttpPort();

        if (port == -1) return;

        SoraFramework framework = new SoraLauncher()
            .setPort(port)
            .setImplementation(HttpServerImplementation.UNDERTOW)
            .buildWithoutPluginLoader();

        framework
            .getSora()
            .register(new AthenaSoraAdapter());

        framework.startHttpServer();
    }

    public static class AthenaSoraAdapter extends SoraPlugin {

        @Override
        public void onInit(Sora sora) {
            sora.addHttpProvider(this, new StreamingRoute());
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
