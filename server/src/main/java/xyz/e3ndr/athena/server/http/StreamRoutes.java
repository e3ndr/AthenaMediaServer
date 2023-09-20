package xyz.e3ndr.athena.server.http;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Map;

import co.casterlabs.rakurai.io.http.StandardHttpStatus;
import co.casterlabs.rakurai.io.http.server.HttpResponse;
import co.casterlabs.rakurai.io.http.server.HttpResponse.ResponseContent;
import co.casterlabs.sora.api.http.HttpProvider;
import co.casterlabs.sora.api.http.SoraHttpSession;
import co.casterlabs.sora.api.http.annotations.HttpEndpoint;
import lombok.SneakyThrows;
import xyz.e3ndr.athena.Athena;
import xyz.e3ndr.athena.MediaSession;
import xyz.e3ndr.athena.types.AudioCodec;
import xyz.e3ndr.athena.types.ContainerFormat;
import xyz.e3ndr.athena.types.VideoCodec;
import xyz.e3ndr.athena.types.VideoQuality;
import xyz.e3ndr.athena.types.media.Media;

class StreamRoutes implements HttpProvider {

    @SneakyThrows
    @HttpEndpoint(uri = "/api/media/:mediaId/stream/raw")
    public HttpResponse onStream(SoraHttpSession session) {
        Map<String, String> query = session.getQueryParameters();

        // Parameters.
        Media media = Athena.getMedia(session.getUriParameters().get("mediaId"));

        VideoQuality videoQuality = VideoQuality.valueOf(query.getOrDefault("quality", VideoQuality.SOURCE.name()).toUpperCase());
        VideoCodec videoCodec = VideoCodec.valueOf(query.getOrDefault("videoCodec", VideoCodec.H264_BASELINE.name()).toUpperCase());
        AudioCodec audioCodec = AudioCodec.valueOf(query.getOrDefault("audioCodec", AudioCodec.SOURCE.name()).toUpperCase());
        ContainerFormat containerFormat = ContainerFormat.valueOf(query.getOrDefault("format", ContainerFormat.MKV.name()).toUpperCase());
        long skipTo = Integer.parseInt(query.getOrDefault("skipTo", "-1"));

        // Parse out the streamIds.
        int[] streamIds = null;

        if (query.containsKey("streams")) {
            String[] split = query.get("streams").split(",");
            streamIds = new int[split.length];

            for (int idx = 0; idx < split.length; idx++) {
                streamIds[idx] = Integer.parseInt(split[idx].trim());
            }
        } else {
            streamIds = media.getFiles().getStreams().getDefaultStreams();
        }

        // Making this a huge number prevents browsers from seeking to the end of the
        // file in search of a time header/atom/embl cue. They will fallback on a
        // brute-force approach where they guess which byte to start at.
        final long fileLength = Long.MAX_VALUE;

        // Figure out the requested range, if any.
        long startAt = 0;
        long endAt = fileLength - 1;
        boolean requestedRange = false;

        String rangeHeader = session.getHeader("range");
        if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
            String[] parts = rangeHeader.substring("bytes=".length()).split("-");

            requestedRange = true;
            startAt = Long.parseLong(parts[0]);
            if (parts.length > 1) {
                endAt = Long.parseLong(parts[1]);
            }
        }

        long chunkLength = endAt - startAt;

        // Response.
        String mimeType;
        if (containerFormat == ContainerFormat.MKV) {
            mimeType = "video/x-matroska";
        } else {
            mimeType = String.format("video/%s", containerFormat.name()).toLowerCase();
        }

        // Load the file.
        MediaSession mediaSession = Athena.startStream(
            media,
            videoQuality,
            videoCodec, audioCodec,
            containerFormat,
            streamIds
        );

        long $_startAt = startAt;
        HttpResponse resp = new HttpResponse(
            new ResponseContent() {
                @Override
                public void write(OutputStream out) {
                    // We do our own write routines.
                    mediaSession.start($_startAt, chunkLength, skipTo, out);
                }

                @Override
                public long getLength() {
                    return chunkLength;
                }

                @Override
                public void close() throws IOException {} // Already handled in MediaSession#start
            },
            requestedRange ? StandardHttpStatus.PARTIAL_CONTENT : StandardHttpStatus.OK
        )
            .setMimeType(mimeType)
            .putHeader("Accept-Ranges", "bytes")
            .putHeader("Cache-Control", "no-store, no-cache, no-transform")
            .putHeader("ETag", Integer.toHexString(media.getId().hashCode()));

        if (requestedRange) {
            resp.putHeader("Content-Range", String.format("bytes %d-%d/%d", startAt, endAt, fileLength));
        }

        return resp
            .putHeader("Access-Control-Allow-Origin", session.getHeaders().getOrDefault("Origin", Arrays.asList("*")).get(0));
    }

    @SneakyThrows
    @HttpEndpoint(uri = "/api/media/:mediaId/stream/hls")
    public HttpResponse onStreamHLS(SoraHttpSession session) {
        final double DURATION = 8;

        String playlist = "#EXTM3U\r\n"
            + "#EXT-X-PLAYLIST-TYPE:VOD\r\n"
            + "#EXT-X-TARGETDURATION:" + DURATION + "\r\n"
            + "#EXT-X-VERSION:4\r\n"
            + "#EXT-X-MEDIA-SEQUENCE:0\r\n";

        String mediaId = session.getUriParameters().get("mediaId");

        // TODO
        playlist += String.format("#EXTINF:%.1f,\r\n/api/media/%s/stream/raw%s&skipTo=%d\r\n", DURATION, mediaId, session.getQueryString(), -1);

        playlist += "#EXT-X-ENDLIST";

        return HttpResponse.newFixedLengthResponse(StandardHttpStatus.OK, playlist)
            .setMimeType("application/vnd.apple.mpegurl");
    }

}
