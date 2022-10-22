package xyz.e3ndr.athena.server.http;

import java.io.IOException;
import java.io.InputStream;
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
import xyz.e3ndr.athena.types.AudioCodec;
import xyz.e3ndr.athena.types.ContainerFormat;
import xyz.e3ndr.athena.types.VideoCodec;
import xyz.e3ndr.athena.types.VideoQuality;
import xyz.e3ndr.athena.types.media.Media;
import xyz.e3ndr.reflectionlib.ReflectionLib;

class StreamingRoute implements HttpProvider {

    @SneakyThrows
    @HttpEndpoint(uri = "/v1/athena/media/:mediaId/stream/raw")
    public HttpResponse onStream(SoraHttpSession session) {
        Map<String, String> query = session.getQueryParameters();

        // Parameters.
        Media media = Athena.getMedia(session.getUriParameters().get("mediaId"));

        long startAt = Long.parseLong(query.getOrDefault("startAt", "0"));
        VideoQuality videoQuality = VideoQuality.valueOf(query.getOrDefault("quality", "SOURCE").toUpperCase());
        VideoCodec videoCodec = VideoCodec.valueOf(query.getOrDefault("videoCodec", "H264").toUpperCase());
        AudioCodec audioCodec = AudioCodec.valueOf(query.getOrDefault("audioCodec", "SOURCE").toUpperCase());
        ContainerFormat containerFormat = ContainerFormat.valueOf(query.getOrDefault("format", "MP4").toUpperCase());

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

        // Video
        InputStream videoInputStream = Athena.startStream(
            media,
            startAt,
            videoQuality, videoCodec, audioCodec, containerFormat,
            streamIds
        );

        // Response.
        String mimeType = String.format("video/%s", containerFormat.name()).toLowerCase();
        if (containerFormat == ContainerFormat.MKV) {
            mimeType = mimeType.replace("mkv", "x-matroska");
        }

        HttpResponse resp = HttpResponse
            .newFixedLengthResponse(StandardHttpStatus.OK, HttpResponse.EMPTY_BODY)
            .setMimeType(mimeType);

        // Intercept the OutputStream.
        ReflectionLib.setValue(resp, "content", new ResponseContent<Void>() {
            @Override
            public void write(OutputStream out) {
                try (videoInputStream) {
                    byte[] buffer = new byte[Athena.STREAMING_BUFFER_SIZE];
                    int read = 0;

                    while ((read = videoInputStream.read(buffer)) != -1) {
                        out.write(buffer, 0, read);
                        out.flush();
                    }
                } catch (IOException ignored) {}
            }

            @Override
            public TransferEncoding getEncoding() {
                return TransferEncoding.CHUNKED;
            }

            @Override
            public long getLength() {
                return Long.MAX_VALUE;
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
                        + "        body {\r\n"
                        + "            margin: 0;\r\n"
                        + "            width: 100%%;\r\n"
                        + "            height: 100%%;\r\n"
                        + "            overflow: hidden;\r\n"
                        + "            background-color: black;\r\n"
                        + "        }\r\n"
                        + "        \r\n"
                        + "        video {\r\n"
                        + "            width: 100%%;\r\n"
                        + "            height: 100%%;\r\n"
                        + "            object-fit: contain;\r\n"
                        + "        }\r\n"
                        + "    </style>\r\n"
                        + "    <video src=\"raw%s\" controls></video>\r\n"
                        + "</html>",
                    session.getQueryString()
                )
            )
            .setMimeType("text/html");
    }

}
