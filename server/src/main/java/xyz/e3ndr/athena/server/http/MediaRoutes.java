package xyz.e3ndr.athena.server.http;

import java.util.Arrays;

import co.casterlabs.rakurai.io.http.StandardHttpStatus;
import co.casterlabs.rakurai.io.http.server.HttpResponse;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.sora.api.http.HttpProvider;
import co.casterlabs.sora.api.http.SoraHttpSession;
import co.casterlabs.sora.api.http.annotations.HttpEndpoint;
import xyz.e3ndr.athena.Athena;
import xyz.e3ndr.athena.types.media.Media;

class MediaRoutes implements HttpProvider {

    @HttpEndpoint(uri = "/api/media")
    public HttpResponse onListMedia(SoraHttpSession session) {
        return HttpResponse
            .newFixedLengthResponse(
                StandardHttpStatus.OK,
                Rson.DEFAULT.toJson(Athena.listMedia())
            )
            .setMimeType("application/json")
            .putHeader("Access-Control-Allow-Origin", session.getHeaders().getOrDefault("Origin", Arrays.asList("*")).get(0));
    }

    @HttpEndpoint(uri = "/api/media/:mediaId")
    public HttpResponse onGetMediaById(SoraHttpSession session) {
        Media media = Athena.getMedia(session.getUriParameters().get("mediaId"));

        return HttpResponse
            .newFixedLengthResponse(
                StandardHttpStatus.OK,
                Rson.DEFAULT.toJson(media)
            )
            .setMimeType("application/json")
            .putHeader("Access-Control-Allow-Origin", session.getHeaders().getOrDefault("Origin", Arrays.asList("*")).get(0));
    }

}
