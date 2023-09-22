package xyz.e3ndr.athena.webui;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.unbescape.html.HtmlEscape;

import co.casterlabs.rakurai.io.http.HttpStatus;
import co.casterlabs.rakurai.io.http.server.HttpResponse;

/**
 * This class is chainable.
 */
public class HTMLBuilder {
    private List<String> lines = new LinkedList<>();

    public HTMLBuilder f(String format, Object... args) {
        // Escape the args.
        for (int idx = 0; idx < args.length; idx++) {
            if (args[idx] instanceof String) {
                args[idx] = HtmlEscape.escapeHtml5(String.valueOf(args[idx]));
            }
        }
        this.lines.add(String.format(format, args));  // Format and add to body.
        return this; // Chain
    }

    @Override
    public String toString() {
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
        lines.addAll(this.lines);
        lines.addAll(
            Arrays.asList(
                "   </body>",
                "</html>"
            )
        );
        return String.join("\r\n", lines);
    }

    public HttpResponse toResponse(HttpStatus status) {
        return HttpResponse
            .newFixedLengthResponse(status, this.toString())
            .setMimeType("text/html");
    }

    public static HTMLBuilder of(String... lines) {
        HTMLBuilder instance = new HTMLBuilder();
        for (String line : lines) instance.lines.add(line);
        return instance;
    }

}
