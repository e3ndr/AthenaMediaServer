package xyz.e3ndr.athena.server.http;

import co.casterlabs.rakurai.io.http.HttpResponse;
import co.casterlabs.rakurai.io.http.StandardHttpStatus;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.sora.api.http.HttpProvider;
import co.casterlabs.sora.api.http.SoraHttpSession;
import co.casterlabs.sora.api.http.annotations.HttpEndpoint;
import xyz.e3ndr.athena.Athena;

class ApiRoutes implements HttpProvider {

    @HttpEndpoint(uri = "/v1/athena/api/transcodes")
    public HttpResponse onListTranscodes(SoraHttpSession session) {
        return HttpResponse.newFixedLengthResponse(
            StandardHttpStatus.OK,
            Rson.DEFAULT.toJson(Athena.transcodeSessions)
        );
    }

}
