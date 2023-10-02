package xyz.e3ndr.athena.service.http;

import co.casterlabs.rakurai.io.http.StandardHttpStatus;
import co.casterlabs.rakurai.io.http.server.HttpResponse;
import co.casterlabs.sora.api.http.HttpProvider;
import co.casterlabs.sora.api.http.SoraHttpSession;
import co.casterlabs.sora.api.http.annotations.HttpEndpoint;
import xyz.e3ndr.athena.Athena;

class MetaRoutes implements HttpProvider {

    @HttpEndpoint(uri = "/.well-known/x-athena")
    public HttpResponse onWellKnown(SoraHttpSession session) {
        return HttpResponse
            .newFixedLengthResponse(StandardHttpStatus.OK, "yes")
            .setMimeType("text/plain");
    }

    @HttpEndpoint(uri = "/*")
    public HttpResponse onGetIndex(SoraHttpSession session) {
        if (Athena.config.services.simpleUI.enable) {
            return HttpResponse
                .newFixedLengthResponse(
                    StandardHttpStatus.NOT_FOUND,
                    "There's nothing here....."
                )
                .setMimeType("text/plain");
        } else {
            return HttpResponse
                .newFixedLengthResponse(
                    StandardHttpStatus.NOT_FOUND,
                    "There's nothing here..... Are you looking for the UI? If so, that's on port " + Athena.config.services.simpleUI.port + "."
                )
                .setMimeType("text/plain");
        }
    }

}
