package xyz.e3ndr.athena.server.http;

import co.casterlabs.rakurai.io.http.StandardHttpStatus;
import co.casterlabs.rakurai.io.http.server.HttpResponse;
import co.casterlabs.sora.api.http.HttpProvider;
import co.casterlabs.sora.api.http.SoraHttpSession;
import co.casterlabs.sora.api.http.annotations.HttpEndpoint;
import xyz.e3ndr.athena.Launcher;

class MetaRoutes implements HttpProvider {

    @HttpEndpoint(uri = "/.well-known/x-athena")
    public HttpResponse onWellKnown(SoraHttpSession session) {
        return HttpResponse
            .newFixedLengthResponse(StandardHttpStatus.OK, "yes")
            .setMimeType("text/plain");
    }

    @HttpEndpoint(uri = "/*")
    public HttpResponse onGetIndex(SoraHttpSession session) {
        int webUiPort = Launcher.getConfig().getWebUiPort();

        if (webUiPort == -1) {
            return HttpResponse
                .newFixedLengthResponse(
                    StandardHttpStatus.OK,
                    "There's nothing here....."
                )
                .setMimeType("text/plain");
        } else {
            return HttpResponse
                .newFixedLengthResponse(
                    StandardHttpStatus.OK,
                    "There's nothing here..... Are you looking for the UI? If so, that's on port " + webUiPort + "."
                )
                .setMimeType("text/plain");
        }
    }

}
