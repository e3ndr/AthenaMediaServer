package xyz.e3ndr.athena.service.special;

import co.casterlabs.rhs.protocol.StandardHttpStatus;
import co.casterlabs.rhs.server.HttpResponse;
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
