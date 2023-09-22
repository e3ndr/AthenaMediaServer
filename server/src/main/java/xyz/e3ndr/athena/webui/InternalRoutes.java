package xyz.e3ndr.athena.webui;

import co.casterlabs.rakurai.io.http.StandardHttpStatus;
import co.casterlabs.rakurai.io.http.server.HttpResponse;
import co.casterlabs.sora.api.http.HttpProvider;
import co.casterlabs.sora.api.http.SoraHttpSession;
import co.casterlabs.sora.api.http.annotations.HttpEndpoint;
import xyz.e3ndr.athena.server.http.StreamRoutes;

class InternalRoutes implements HttpProvider {
    private StreamRoutes streamRoutes = new StreamRoutes();

    @HttpEndpoint(uri = "/_internal/media/:mediaId/stream/raw")
    public HttpResponse onStreamRAW(SoraHttpSession session) {
        return this.streamRoutes.onStreamRAW(session);
    }

    @HttpEndpoint(uri = "/_internal/media/:mediaId/stream/hls")
    public HttpResponse onStreamHLS(SoraHttpSession session) {
        String playlist = this.streamRoutes.generateHLSPlaylist(session, "/_internal/media/%s/stream/raw");
        return HttpResponse.newFixedLengthResponse(StandardHttpStatus.OK, playlist)
            .setMimeType("application/vnd.apple.mpegurl");
    }

}
