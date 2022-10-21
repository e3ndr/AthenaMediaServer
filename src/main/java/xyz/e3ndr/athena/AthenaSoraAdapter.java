package xyz.e3ndr.athena;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.sora.Sora;
import co.casterlabs.sora.api.SoraPlugin;
import lombok.NonNull;
import xyz.e3ndr.athena.routes.StreamingRoute;

public class AthenaSoraAdapter extends SoraPlugin {

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
