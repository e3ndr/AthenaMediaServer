package xyz.e3ndr.athena.service.special;

import co.casterlabs.rakurai.io.http.server.HttpResponse;
import co.casterlabs.sora.api.http.HttpProvider;
import co.casterlabs.sora.api.http.SoraHttpSession;
import co.casterlabs.sora.api.http.annotations.HttpEndpoint;
import xyz.e3ndr.athena.service.MediaStreamRoutes;

class InternalRoutes implements HttpProvider {
    private MediaStreamRoutes streamRoutes = new MediaStreamRoutes();

    @HttpEndpoint(uri = "/_internal/media/:mediaId/stream")
    public HttpResponse onStreamRAW(SoraHttpSession session) {
        return this.streamRoutes.onStreamRAW(session);
    }

}
