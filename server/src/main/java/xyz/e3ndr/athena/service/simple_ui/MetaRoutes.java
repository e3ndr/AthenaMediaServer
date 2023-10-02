package xyz.e3ndr.athena.service.simple_ui;

import co.casterlabs.rakurai.io.http.StandardHttpStatus;
import co.casterlabs.rakurai.io.http.server.HttpResponse;
import co.casterlabs.sora.api.http.HttpProvider;
import co.casterlabs.sora.api.http.SoraHttpSession;
import co.casterlabs.sora.api.http.annotations.HttpEndpoint;

class MetaRoutes implements HttpProvider {

    @HttpEndpoint(uri = "/*")
    public HttpResponse onGetMissing(SoraHttpSession session) {
        return HttpResponse
            .newFixedLengthResponse(
                StandardHttpStatus.NOT_FOUND,
                "There's nothing here....."
            )
            .setMimeType("text/plain");
    }

}
