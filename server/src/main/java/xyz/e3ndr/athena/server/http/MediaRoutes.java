package xyz.e3ndr.athena.server.http;

import java.util.Arrays;
import java.util.Map;

import co.casterlabs.rakurai.io.http.StandardHttpStatus;
import co.casterlabs.rakurai.io.http.server.HttpResponse;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonObject;
import co.casterlabs.sora.api.http.HttpProvider;
import co.casterlabs.sora.api.http.SoraHttpSession;
import co.casterlabs.sora.api.http.annotations.HttpEndpoint;
import xyz.e3ndr.athena.Athena;
import xyz.e3ndr.athena.types.media.Media;

class MediaRoutes implements HttpProvider {

    @HttpEndpoint(uri = "/api/media")
    public HttpResponse onListMedia(SoraHttpSession session) {
        return new JsonResponse(
            StandardHttpStatus.OK,
            JsonObject.singleton("list", Rson.DEFAULT.toJson(Athena.listMedia())),
            Map.of("media", "GET /api/media/:mediaId")
        )
            .putHeader("Access-Control-Allow-Origin", session.getHeaders().getOrDefault("Origin", Arrays.asList("*")).get(0));
    }

    @HttpEndpoint(uri = "/api/media/:mediaId")
    public HttpResponse onGetMediaById(SoraHttpSession session) {
        Media media = Athena.getMedia(session.getUriParameters().get("mediaId"));

        return new JsonResponse(
            StandardHttpStatus.OK,
            Rson.DEFAULT.toJson(media),
            Map.of(
                "stream_raw", "GET /api/media/:mediaId/stream/raw?quality&videoCodec&audioCodec&format&skipTo",
                "stream_hls", "GET /api/media/:mediaId/stream/hls?quality&videoCodec&audioCodec&format&skipTo"
            )
        )
            .putHeader("Access-Control-Allow-Origin", session.getHeaders().getOrDefault("Origin", Arrays.asList("*")).get(0));
    }

}
