package xyz.e3ndr.athena.server.http;

import java.util.Arrays;
import java.util.Map;

import co.casterlabs.rakurai.io.http.StandardHttpStatus;
import co.casterlabs.rakurai.io.http.server.HttpResponse;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.sora.api.http.HttpProvider;
import co.casterlabs.sora.api.http.SoraHttpSession;
import co.casterlabs.sora.api.http.annotations.HttpEndpoint;
import xyz.e3ndr.athena.Athena;

class SessionRoutes implements HttpProvider {

    @HttpEndpoint(uri = "/api/sessions/transcodes")
    public HttpResponse onListTranscodes(SoraHttpSession session) {
        return new JsonResponse(
            StandardHttpStatus.OK,
            Rson.DEFAULT.toJson(Athena.transcodeSessions),
            Map.of("playbacks", "GET /api/sessions/playbacks")
        )
            .putHeader("Access-Control-Allow-Origin", session.getHeaders().getOrDefault("Origin", Arrays.asList("*")).get(0));
    }

    @HttpEndpoint(uri = "/api/sessions/playbacks")
    public HttpResponse onListPlaybacks(SoraHttpSession session) {
        return new JsonResponse(
            StandardHttpStatus.OK,
            Rson.DEFAULT.toJson(Athena.mediaSessions),
            Map.of("transcodes", "GET /api/sessions/transcodes")
        )
            .putHeader("Access-Control-Allow-Origin", session.getHeaders().getOrDefault("Origin", Arrays.asList("*")).get(0));
    }

}
