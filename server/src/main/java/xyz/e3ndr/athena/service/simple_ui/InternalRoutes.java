package xyz.e3ndr.athena.service.simple_ui;

import co.casterlabs.rhs.server.HttpResponse;
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

    @HttpEndpoint(uri = "/_internal/media/:mediaId/stream/hls/:file")
    public HttpResponse onStreamHLS(SoraHttpSession session) {
        return this.streamRoutes.onStreamHLS(session);
    }

}
