package xyz.e3ndr.athena.server.http;

import co.casterlabs.rakurai.io.http.HttpResponse;
import co.casterlabs.rakurai.io.http.StandardHttpStatus;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.sora.api.http.HttpProvider;
import co.casterlabs.sora.api.http.SoraHttpSession;
import co.casterlabs.sora.api.http.annotations.HttpEndpoint;
import xyz.e3ndr.athena.Athena;

class SessionRoutes implements HttpProvider {

    @HttpEndpoint(uri = "/api/transcodes")
    public HttpResponse onListTranscodes(SoraHttpSession session) {
        return HttpResponse.newFixedLengthResponse(
            StandardHttpStatus.OK,
            Rson.DEFAULT.toJson(Athena.transcodeSessions)
        );
    }

    @HttpEndpoint(uri = "/sessions")
    public HttpResponse onListSessions(SoraHttpSession session) {
        return HttpResponse.newFixedLengthResponse(
            StandardHttpStatus.OK,
            Rson.DEFAULT.toJson(Athena.mediaSessions)
        );
    }

}
