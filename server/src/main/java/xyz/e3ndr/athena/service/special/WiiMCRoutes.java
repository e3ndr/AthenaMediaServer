package xyz.e3ndr.athena.service.special;

import java.util.LinkedList;
import java.util.List;

import co.casterlabs.rhs.protocol.StandardHttpStatus;
import co.casterlabs.rhs.server.HttpResponse;
import co.casterlabs.sora.api.http.HttpProvider;
import co.casterlabs.sora.api.http.SoraHttpSession;
import co.casterlabs.sora.api.http.annotations.HttpEndpoint;
import xyz.e3ndr.athena.Athena;
import xyz.e3ndr.athena.types.media.Media;

class WiiMCRoutes implements HttpProvider {

    @HttpEndpoint(uri = "/wiimc/list")
    public HttpResponse onList(SoraHttpSession session) {
        List<Media> mediaList = Athena.listMedia(0, Integer.MAX_VALUE);

        String playlistResponse = generatePlaylistForMedia(session.getHost(), mediaList);

        return HttpResponse.newFixedLengthResponse(
            StandardHttpStatus.OK,
            playlistResponse
        )
            .setMimeType("text/plain");
    }

    @HttpEndpoint(uri = "/wiimc/search")
    public HttpResponse onSearch(SoraHttpSession session) {
        String query = session.getQueryParameters().getOrDefault("q", "");
        List<Media> mediaList = Athena.searchMedia(query);

        String playlistResponse = generatePlaylistForMedia(session.getHost(), mediaList);

        return HttpResponse.newFixedLengthResponse(
            StandardHttpStatus.OK,
            playlistResponse
        )
            .setMimeType("text/plain");
    }

    private static String generatePlaylistForMedia(String host, List<Media> mediaList) {
        List<String> playlistResponse = new LinkedList<>();
        playlistResponse.add("#EXTM3U");

        for (Media media : mediaList) {
            String url = String.format(
                "http://%s/_internal/media/%s/stream/raw?quality=SD&format=MKV&videoCodec=H264_BASELINE&audioCodec=AAC&subtitleCodec=ASS",
                host, media.getId()
            );

            playlistResponse.add(String.format("#EXTINF:-1=\"%s\",%s", media.getFiles().getImages().getPosterUrl(), media.toString()));
            playlistResponse.add(url);
        }

        return String.join("\r\n", playlistResponse);
    }

}
