package xyz.e3ndr.athena.service;

import java.io.File;
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

// Used by Http, Simple UI, & Special
public class MediaStreamRoutes implements HttpProvider {

    private MediaSession startSession(Media media, Map<String, String> query) throws IOException {
        VideoQuality videoQuality = VideoQuality.valueOf(query.getOrDefault("quality", VideoQuality.UHD.name()).toUpperCase());
        VideoCodec videoCodec = VideoCodec.valueOf(query.getOrDefault("videoCodec", VideoCodec.SOURCE.name()).toUpperCase());
        AudioCodec audioCodec = AudioCodec.valueOf(query.getOrDefault("audioCodec", AudioCodec.SOURCE.name()).toUpperCase());
        ContainerFormat containerFormat = ContainerFormat.valueOf(query.getOrDefault("format", ContainerFormat.MKV.name()).toUpperCase());

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

        // Load the file.
        return Athena.startStream(
            media,
            videoQuality,
            videoCodec, audioCodec,
            containerFormat,
            streamIds
        );
    }

    @SneakyThrows
    @HttpEndpoint(uri = "/api/media/:mediaId/stream")
    public HttpResponse onStreamRAW(SoraHttpSession session) {
        Media media = Athena.getMedia(session.getUriParameters().get("mediaId"));
        MediaSession mediaSession = startSession(media, session.getQueryParameters());

        String mimeType;
        if (mediaSession.getContainerFormat() == ContainerFormat.MKV) {
            mimeType = "video/x-matroska";
        } else {
            mimeType = String.format("video/%s", mediaSession.getContainerFormat().name()).toLowerCase();
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

        long $_startAt = startAt;
        HttpResponse resp = new HttpResponse(
            new ResponseContent() {
                @Override
                public void write(OutputStream out) {
                    // We do our own write routines.
                    mediaSession.start($_startAt, chunkLength, out);
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
            .putHeader("ETag", Integer.toHexString(mediaSession.getMediaId().hashCode()));

        if (requestedRange) {
            resp.putHeader("Content-Range", String.format("bytes %d-%d/%d", startAt, endAt, fileLength));
        }

        return resp
            .putHeader("Access-Control-Allow-Origin", session.getHeaders().getOrDefault("Origin", Arrays.asList("*")).get(0));
    }

    @SneakyThrows
    @HttpEndpoint(uri = "/api/media/:mediaId/stream/hls/:file")
    public HttpResponse onStreamHLS(SoraHttpSession session) {
        VideoQuality videoQuality = VideoQuality.valueOf(session.getQueryParameters().getOrDefault("quality", VideoQuality.UHD.name()).toUpperCase());
        VideoCodec videoCodec = VideoCodec.valueOf(session.getQueryParameters().getOrDefault("videoCodec", VideoCodec.H264_HIGH.name()).toUpperCase());
        AudioCodec audioCodec = AudioCodec.valueOf(session.getQueryParameters().getOrDefault("audioCodec", AudioCodec.AAC.name()).toUpperCase());
        ContainerFormat containerFormat = ContainerFormat.HLS;

        Map<String, String> query = Map.of(
            "quality", videoQuality.name(),
            "videoCodec", videoCodec.name(),
            "audioCodec", audioCodec.name(),
            "format", containerFormat.name()
        );

        Media media = Athena.getMedia(session.getUriParameters().get("mediaId"));
        MediaSession mediaSession = startSession(media, query); // Dummy session.

        // TODO Figure out a way to make the dummy session persist in Athena's session
        // list.

        File file = new File(mediaSession.getFile(), session.getUriParameters().get("file"));

        while (!file.exists()) {
            Thread.sleep(5000); // Wait.
        }

        return HttpResponse.newFixedLengthFileResponse(StandardHttpStatus.OK, file);
    }

}
