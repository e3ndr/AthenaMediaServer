package xyz.e3ndr.athena.webui;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import co.casterlabs.rakurai.io.http.StandardHttpStatus;
import co.casterlabs.rakurai.io.http.server.HttpResponse;
import co.casterlabs.sora.api.http.HttpProvider;
import co.casterlabs.sora.api.http.SoraHttpSession;
import co.casterlabs.sora.api.http.annotations.HttpEndpoint;

class UIRoutes implements HttpProvider {

    @HttpEndpoint(uri = "/*")
    public HttpResponse onViewIndex(SoraHttpSession session) {
        return html(
            "<h1>Welcome to Athena!</h1>",
        );
    }

    private static HttpResponse html(String... body) {
        List<String> lines = new LinkedList<>();
        lines.addAll(
            Arrays.asList(
                "<!DOCTYPE html>",
                "<html>",
                "   <head>",
                "       <meta charset=\"utf-8\" />",
                "       <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\" />",
                "       <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />",
                "       <title>Athena</title>",
                "   </head>",
                "   <body>"
            )
        );
        lines.addAll(Arrays.asList(body));
        lines.addAll(
            Arrays.asList(
                "   </body>",
                "</html>"
            )
        );
        return HttpResponse
            .newFixedLengthResponse(StandardHttpStatus.OK, String.join("\r\n", lines))
            .setMimeType("text/html");
    }

}
