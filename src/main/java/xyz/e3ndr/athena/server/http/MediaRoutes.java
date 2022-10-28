package xyz.e3ndr.athena.server.http;

import java.io.OutputStream;
import java.util.Map;

import co.casterlabs.rakurai.io.http.HttpResponse;
import co.casterlabs.rakurai.io.http.HttpResponse.ResponseContent;
import co.casterlabs.rakurai.io.http.HttpResponse.TransferEncoding;
import co.casterlabs.rakurai.io.http.StandardHttpStatus;
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
import xyz.e3ndr.reflectionlib.ReflectionLib;

class MediaRoutes implements HttpProvider {

    @SneakyThrows
    @HttpEndpoint(uri = "/v1/athena/media/:mediaId/stream/raw")
    public HttpResponse onStream(SoraHttpSession session) {
        Map<String, String> query = session.getQueryParameters();

        // Parameters.
        Media media = Athena.getMedia(session.getUriParameters().get("mediaId"));

        VideoQuality videoQuality = VideoQuality.valueOf(query.getOrDefault("quality", "SOURCE").toUpperCase());
        VideoCodec videoCodec = VideoCodec.valueOf(query.getOrDefault("videoCodec", "H264").toUpperCase());
        AudioCodec audioCodec = AudioCodec.valueOf(query.getOrDefault("audioCodec", "SOURCE").toUpperCase());
        ContainerFormat containerFormat = ContainerFormat.valueOf(query.getOrDefault("format", "MKV").toUpperCase());
        String forceMime = query.get("forceMime");

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

        if (forceMime != null) {
            mimeType = forceMime;
        } else if (containerFormat == ContainerFormat.MKV) {
            mimeType = "video/x-matroska";
        } else {
            mimeType = String.format("video/%s", containerFormat.name()).toLowerCase();
        }

        HttpResponse resp = HttpResponse
            .newFixedLengthResponse(
                requestedRange ? StandardHttpStatus.PARTIAL_CONTENT : StandardHttpStatus.OK,
                HttpResponse.EMPTY_BODY
            )
            .setMimeType(mimeType)
            .putHeader("Accept-Ranges", "bytes")
            .putHeader("Cache-Control", "no-store, no-cache, no-transform")
            .putHeader("ETag", Integer.toHexString(media.getId().hashCode()));

        if (requestedRange) {
            resp.putHeader("Content-Range", String.format("bytes %d-%d/%d", startAt, endAt, fileLength));
        }

        // Load the file.
        MediaSession mediaSession = Athena.startStream(
            media,
            videoQuality,
            videoCodec, audioCodec,
            containerFormat,
            streamIds
        );

        // Intercept the OutputStream, do our own write routines.
        long $_startAt = startAt;
        ReflectionLib.setValue(resp, "content", new ResponseContent<Void>() {
            @Override
            public void write(OutputStream out) {
                mediaSession.start($_startAt, chunkLength, out);
            }

            @Override
            public TransferEncoding getEncoding() {
                return TransferEncoding.FIXED_LENGTH;
            }

            @Override
            public long getLength() {
                return chunkLength;
            }

            @Override
            public Void raw() {
                return null;
            }
        });

        return resp;
    }

    @HttpEndpoint(uri = "/v1/athena/media/:mediaId/stream/html5")
    public HttpResponse onStreamHtml(SoraHttpSession session) {
        return HttpResponse
            .newFixedLengthResponse(
                StandardHttpStatus.OK,
                String.format(
                    "<!DOCTYPE html>\r\n"
                        + "<html>\r\n"
                        + "    <style>\r\n"
                        + "        html {\r\n"
                        + "            height: 100%%;\r\n"
                        + "            background-color: black;\r\n"
                        + "        }\r\n"
                        + "        body {\r\n"
                        + "            margin: 0;\r\n"
                        + "            width: 100%%;\r\n"
                        + "            height: 100%%;\r\n"
                        + "            overflow: hidden;\r\n"
                        + "        }\r\n"
                        + "        \r\n"
                        + "        video {\r\n"
                        + "            width: 100%%;\r\n"
                        + "            height: 100%%;\r\n"
                        + "            object-fit: contain;\r\n"
                        + "        }\r\n"
                        + "    </style>\r\n"
                        + "    <video src=\"raw%s\" onerror=\"console.error('Error:', this.error)\" controls></video>\r\n"
                        + "</html>",
                    session.getQueryString()
                )
            )
            .setMimeType("text/html");
    }

}
