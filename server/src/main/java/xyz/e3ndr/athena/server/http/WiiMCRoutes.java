package xyz.e3ndr.athena.server.http;

import java.util.List;

import co.casterlabs.rakurai.io.http.StandardHttpStatus;
import co.casterlabs.rakurai.io.http.server.HttpResponse;
import co.casterlabs.sora.api.http.HttpProvider;
import co.casterlabs.sora.api.http.SoraHttpSession;
import co.casterlabs.sora.api.http.annotations.HttpEndpoint;
import xyz.e3ndr.athena.Athena;
import xyz.e3ndr.athena.types.media.Media;

class WiiMCRoutes implements HttpProvider {

    @HttpEndpoint(uri = "/api/wiimc/list")
    public HttpResponse onList(SoraHttpSession session) {
        List<Media> mediaList = Athena.listMedia();

        String playlistResponse = generatePlaylistForMedia(session.getHost(), mediaList);

        return HttpResponse.newFixedLengthResponse(
            StandardHttpStatus.OK,
            playlistResponse
        )
            .setMimeType("text/plain");
    }

    @HttpEndpoint(uri = "/api/wiimc/search")
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
        StringBuilder playlistResponse = new StringBuilder();
        playlistResponse.append("[Playlist]\r\n");

        int idx = 1;
        for (Media media : mediaList) {
            String url = String.format(
                "http://%s/api/media/%s/stream/raw?quality=SD&format=MKV&videoCodec=H264&audioCodec=AAC",
                host, media.getId()
            );

            playlistResponse.append(String.format("File%d=%s\r\n", idx, url));
            playlistResponse.append(String.format("Title%d=%s\r\n", idx, media.toString()));
            playlistResponse.append(String.format("Length%d=0\r\n", idx));
            playlistResponse.append("\r\n");
            idx++;
        }

        return playlistResponse.toString();
    }

}
