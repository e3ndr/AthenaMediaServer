package xyz.e3ndr.athena.webui;

import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import org.unbescape.uri.UriEscape;

import co.casterlabs.rakurai.io.IOUtil;
import co.casterlabs.rakurai.io.http.StandardHttpStatus;
import co.casterlabs.rakurai.io.http.server.HttpResponse;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.TypeToken;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.element.JsonObject;
import co.casterlabs.sora.api.http.HttpProvider;
import co.casterlabs.sora.api.http.SoraHttpSession;
import co.casterlabs.sora.api.http.annotations.HttpEndpoint;
import lombok.SneakyThrows;
import xyz.e3ndr.athena.Athena;
import xyz.e3ndr.athena.types.AudioCodec;
import xyz.e3ndr.athena.types.ContainerFormat;
import xyz.e3ndr.athena.types.VideoCodec;
import xyz.e3ndr.athena.types.VideoQuality;
import xyz.e3ndr.athena.types.media.Media;

class UIRoutes implements HttpProvider {
    private static final String MEDIA_SEARCH_API = System.getProperty("athena.searchapi", "https://athenamediaserver-public-api.e3ndr.workers.dev/search?query=");

    @SneakyThrows
    private static List<Media> ingest_searchForMedia(String query) {
        URL url = new URL(MEDIA_SEARCH_API + UriEscape.escapeUriFragmentId(query));
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        String result = IOUtil.readInputStreamString(conn.getInputStream(), StandardCharsets.UTF_8);
        JsonObject resultJson = Rson.DEFAULT.fromJson(result, JsonObject.class);
        return Rson.DEFAULT.fromJson(
            resultJson
                .getObject("data")
                .get("found"),
            new TypeToken<List<Media>>() {
            }
        );
    }

    @HttpEndpoint(uri = "/*")
    public HttpResponse onViewIndex(SoraHttpSession session) {
        return HTMLBuilder.of(
            "<h1>Welcome to Athena!</h1>",
            "<p>Continue to your <a href=\"/media\">media library</a>.</p>",
            "<p>Or</p>",
            "<p><a href=\"/ingest\">Ingest media</a>.</p>"
        ).toResponse(StandardHttpStatus.OK);
    }

    @HttpEndpoint(uri = "/ingest")
    public HttpResponse onViewIngest(SoraHttpSession session) {
        HTMLBuilder html = new HTMLBuilder();
        html.f("<a href=\"/\" onclick=\"history.go(-1); return false;\">Go back</a>")
            .f("<br />")
            .f("<br />")
            .f("<p>Select a file to ingest:</p>")
            .f("<form method=\"GET\" action=\"/ingest/media-search\">")
            .f("  <select name=\"file\">");
        for (String ingestable : Athena.listIngestables()) {
            html.f("    <option>%s</option>", ingestable);
        }
        html.f("  </select>")
            .f("  <input type=\"submit\" value=\"Go\" />")
            .f("</form>");
        return html.toResponse(StandardHttpStatus.OK);
    }

    @SneakyThrows
    @HttpEndpoint(uri = "/ingest/media-search")
    public HttpResponse onViewIngestSearch(SoraHttpSession session) {
        String toIngest = session.getQueryParameters().get("file");
        String query = session.getQueryParameters().getOrDefault("query", toIngest);

        List<Media> searchResults = ingest_searchForMedia(query);

        HTMLBuilder html = new HTMLBuilder()
            .f("<a href=\"/ingest\" onclick=\"history.go(-1); return false;\">Go back</a>")
            .f("<br />")
            .f("<br />")
            .f("<h1>%s</h1>", toIngest)
            .f("<h2>Search for a match.</h2>")
            .f("<form method=\"GET\" action=\"/ingest/media-search\">")
            .f("  <input type=\"input\" value=\"%s\" name=\"file\" style=\"display: none;\" />", toIngest)
            .f("  <input type=\"input\" value=\"%s\" name=\"query\" />", query)
            .f("  <input type=\"submit\" value=\"Search\" />")
            .f("</form>")
            .f("<br />")
            .f("<br />");

        if (searchResults.isEmpty()) {
            html.f("<h3>No result.</h3>");
        } else {
            html
                .f("<h3>Select the correct movie:</h3>")
                .f("<form method=\"GET\" action=\"/ingest/map-streams\">")
                .f("  <input type=\"input\" value=\"%s\" name=\"file\" style=\"display: none;\" />", toIngest)
                .f("  <select name=\"media\">");
            for (Media media : searchResults) {
                html.f("    <option value=\"%s\">%s (%d)</option>", UriEscape.escapeUriQueryParam(Rson.DEFAULT.toJson(media).toString()), media.getInfo().getTitle(), media.getInfo().getYear());
            }
            html.f("  </select>")
                .f("  <input type=\"submit\" value=\"Choose\" />")
                .f("</form>");
        }

        return html.toResponse(StandardHttpStatus.OK);
    }

    @SneakyThrows
    @HttpEndpoint(uri = "/ingest/map-streams")
    public HttpResponse onViewIngestMapStreams(SoraHttpSession session) {
        String toIngest = session.getQueryParameters().get("file");
        Media media = Rson.DEFAULT.fromJson(UriEscape.unescapeUriQueryParam(session.getQueryParameters().get("media")), Media.class);

        String genres = String.join(", ", media.getInfo().getGenres());
        String studios = String.join(", ", media.getInfo().getStudios().stream().limit(2).map((s) -> s.getName()).collect(Collectors.toList()));
        String directors = String.join(", ", media.getInfo().getDirectors().stream().map((p) -> p.getName()).collect(Collectors.toList()));
        String actors = String.join(", ", media.getInfo().getActors().stream().limit(4).map((p) -> p.getName()).collect(Collectors.toList()));

        HTMLBuilder html = new HTMLBuilder()
            .f("<a href=\"/ingest\" onclick=\"history.go(-1); return false;\">Go back</a>")
            .f("<br />")
            .f("<br />")
            .f("<table style=\"background: url('%s'); background-size: cover; text-shadow: 0px 0px 4px white;\">", media.getFiles().getImages().getBackdropUrl())
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
            .f("        <h1 style=\"margin: 0px;\">%s (%d) [%s]</h1>", media.getInfo().getTitle(), media.getInfo().getYear(), toIngest)
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
            .f("    </td>")
            .f("  </tr>")
            .f("</table>")
            .f("<br />")
            .f("<br />");

        html
            .f("<h3>Streams</h3>")
            .f("<form method=\"POST\" action=\"/ingest/finalize\">");

        int codecIdx = 0;
        for (JsonElement e : Athena.getIngestableInfo(toIngest)) {
            JsonObject codec = e.getAsObject();

            html
                .f("<h4 style=\"margin-bottom: 0;\">Stream %d</h4>", codecIdx);

            switch (codec.getString("codec_type")) {
                case "video": {
                    String codecName = codec.getString("codec_name").toUpperCase();
                    html
                        .f("Name: <input type=\"input\" name=\"stream/%d/name\" value=\"%s\" />", codecIdx, codecName)
                        .f("Default Video Stream?: <input type=\"radio\" name=\"video/default\" value=\"%d\" checked />", codecIdx, codecIdx);
                    break;
                }

                case "audio": {
                    String codecName = codec.getString("codec_name").toUpperCase();
                    String channelLayout = codec.getString("channel_layout");
                    channelLayout = channelLayout.substring(0, 1).toUpperCase() + channelLayout.substring(1).toLowerCase();
                    html
                        .f("Name: <input type=\"input\" name=\"stream/%d/name\" value=\"%s (%s)\" />", codecIdx, channelLayout, codecName)
                        .f("Default Audio Stream?: <input type=\"radio\" name=\"audio/default\" value=\"%d\" checked />", codecIdx, codecIdx);
                    break;
                }

                // TODO others.
            }
            codecIdx++;
        }

        html
            .f("  <input type=\"input\" value=\"%s\" name=\"file\" style=\"display: none;\" />", toIngest)
            .f("  <input type=\"input\" value=\"%s\" name=\"media\" style=\"display: none;\" />", UriEscape.escapeUriQueryParam(Rson.DEFAULT.toJson(media).toString()))
            .f("  <br />")
            .f("  <br />")
            .f("  <input type=\"submit\" value=\"Ingest!\" />")
            .f("</form>");

        return html.toResponse(StandardHttpStatus.OK);
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
            .f("<table style=\"background: url('%s'); background-size: cover; text-shadow: 0px 0px 4px white;\">", media.getFiles().getImages().getBackdropUrl())
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
