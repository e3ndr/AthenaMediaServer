package xyz.e3ndr.athena.server.http;

import co.casterlabs.rakurai.io.http.StandardHttpStatus;
import co.casterlabs.rakurai.io.http.server.HttpResponse;
import co.casterlabs.sora.api.http.HttpProvider;
import co.casterlabs.sora.api.http.SoraHttpSession;
import co.casterlabs.sora.api.http.annotations.HttpEndpoint;

class MetaRoutes implements HttpProvider {

    @HttpEndpoint(uri = "/.well-known/x-athena")
    public HttpResponse onWellKnown(SoraHttpSession session) {
        return HttpResponse
            .newFixedLengthResponse(StandardHttpStatus.OK, "yes")
            .setMimeType("text/plain");
    }

}
