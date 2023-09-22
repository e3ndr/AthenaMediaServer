package xyz.e3ndr.athena.webui;

import java.util.List;
import java.util.stream.Collectors;

import co.casterlabs.rakurai.io.http.StandardHttpStatus;
import co.casterlabs.rakurai.io.http.server.HttpResponse;
import co.casterlabs.sora.api.http.HttpProvider;
import co.casterlabs.sora.api.http.SoraHttpSession;
import co.casterlabs.sora.api.http.annotations.HttpEndpoint;
import xyz.e3ndr.athena.Athena;
import xyz.e3ndr.athena.types.AudioCodec;
import xyz.e3ndr.athena.types.ContainerFormat;
import xyz.e3ndr.athena.types.VideoCodec;
import xyz.e3ndr.athena.types.VideoQuality;
import xyz.e3ndr.athena.types.media.Media;

class UIRoutes implements HttpProvider {

    @HttpEndpoint(uri = "/*")
    public HttpResponse onViewIndex(SoraHttpSession session) {
        return HTMLBuilder.of(
            "<h1>Welcome to Athena!</h1>",
            "<p>Continue to your <a href=\"/media\">media library</a>.</p>"
        ).toResponse(StandardHttpStatus.OK);
    }

    @HttpEndpoint(uri = "/media")
    public HttpResponse onViewMedia(SoraHttpSession session) {
        final int itemsPerPage = 20;
        int page = Integer.parseInt(session.getQueryParameters().getOrDefault("page", "1"));
        int total = Athena.totalMedia();

        // Constrain.
        if (page < 1) {
            page = 1;
        } else if ((page - 1) * itemsPerPage > total) {
            page = 1;
        }

        List<Media> listOfMedia = Athena.listMedia((page - 1) * itemsPerPage, itemsPerPage);

        HTMLBuilder html = new HTMLBuilder();
        html.f("<a href=\"/\" onclick=\"history.go(-1); return false;\">Go back</a>");
        html.f("<br />");
        html.f("<br />");

        for (Media media : listOfMedia) {
            html.f("<a");
            html.f("  style=\"margin: 10px; display: inline-block\"");
            html.f("  href=\"/media/%s\"", media.getId());
            html.f("  title=\"%s (%d)\"", media.getInfo().getTitle(), media.getInfo().getYear());
            html.f(">");
            html.f("  <img");
            html.f("    style=\"width: 200px; height: 320px; border-radius: 20px; object-fit: cover\"");
            html.f("    src=\"%s\"", media.getFiles().getImages().getPosterUrl());
            html.f("    alt=\"%s Poster\"", media.getInfo().getTitle());
            html.f("  />");
            html.f("  <h1 style=\"font-size: small; font-weight: 500\">%s</h1>", media.getInfo().getTitle());
            html.f("</a>");
        }

        html.f("<br />");
        html.f("<br />");

        html.f("You are on page %d.", page);
        if (page > 1) {
            html.f("<a href=\"?page=%d\">Previous</a>", page - 1);
        }
        if ((page) * itemsPerPage < total) {
            html.f("<a href=\"?page=%d\">Next</a>", page + 1);
        }

        return html.toResponse(StandardHttpStatus.OK);
    }

    @HttpEndpoint(uri = "/media/:mediaId")
    public HttpResponse onViewSpecificMedia(SoraHttpSession session) {
        Media media = Athena.getMedia(session.getUriParameters().get("mediaId"));

        if (media == null) {
            return HttpResponse.newFixedLengthResponse(StandardHttpStatus.NOT_FOUND);
        }

        String genres = String.join(", ", media.getInfo().getGenres());
        String studios = String.join(", ", media.getInfo().getStudios().stream().limit(2).map((s) -> s.getName()).collect(Collectors.toList()));
        String directors = String.join(", ", media.getInfo().getDirectors().stream().map((p) -> p.getName()).collect(Collectors.toList()));
        String actors = String.join(", ", media.getInfo().getActors().stream().limit(4).map((p) -> p.getName()).collect(Collectors.toList()));

        return new HTMLBuilder()
            .f("<a href=\"/media\" onclick=\"history.go(-1); return false;\">Go back</a>")
            .f("<br />")
            .f("<br />")
            .f("<table>")
            .f("  <tr>")
            .f("    <td>")
            .f("      <img")
            .f("        style=\"width: 192px; height: 307px; object-fit: cover; border-radius: 5px; display: inline-block;\"")
            .f("        alt=\"\"")
            .f("        src=\"%s\"", media.getFiles().getImages().getPosterUrl())
            .f("      />")
            .f("    </td>")
            .f("    <td style=\"vertical-align: top; padding-left: 15px;\">")
            .f("      <div>")
            .f("        <h1 style=\"margin: 0px;\">%s (%d)</h1>", media.getInfo().getTitle(), media.getInfo().getYear())
            .f("        <h2 style=\"margin: 0px; font-size: large;\">%s</h2>", genres)
            .f("      </div>")
            .f("")
            .f("      <p>%s</p>", media.getInfo().getSummary())
            .f("")
            .f("      <table>")
            .f("        <tr>")
            .f("          <td>Directors:</td>")
            .f("          <td>%s</td>", directors)
            .f("        </tr>")
            .f("        <tr>")
            .f("          <td>Starring:</td>")
            .f("          <td>%s</td>", actors)
            .f("        </tr>")
            .f("        <tr>")
            .f("          <td>Studios:</td>")
            .f("          <td>%s</td>", studios)
            .f("        </tr>")
            .f("      </table>")
            .f("")
            .f("      <br />")
            .f("      <a href=\"/media/%s/watch\">Watch</a>", media.getId())
            .f("    </td>")
            .f("  </tr>")
            .f("</table>")
            .toResponse(StandardHttpStatus.OK);
    }

    @HttpEndpoint(uri = "/media/:mediaId/watch")
    public HttpResponse onWatchSpecificMedia(SoraHttpSession session) {
        Media media = Athena.getMedia(session.getUriParameters().get("mediaId"));

        if (media == null) {
            return HttpResponse.newFixedLengthResponse(StandardHttpStatus.NOT_FOUND);
        }

        ContainerFormat container = ContainerFormat.MKV;
        VideoCodec vCodec = VideoCodec.SOURCE;
        AudioCodec aCodec = AudioCodec.SOURCE;
        VideoQuality quality = VideoQuality.UHD;

        return new HTMLBuilder()
            .f("<a href=\"/media/%s\" onclick=\"history.go(-1); return false;\">Go back</a>", media.getId())
            .f("<br />")
            .f("<br />")
            .f(
                "<video src=\"/_internal/media/%s/stream/raw?format=%s&videoCodec=%s&audioCodec=%s&quality=%s\" controls fullscreen style=\"width: 100%%; height: 100%%;\" />",
                media.getId(),
                container, vCodec, aCodec, quality
            )
            .toResponse(StandardHttpStatus.OK);
    }

}
