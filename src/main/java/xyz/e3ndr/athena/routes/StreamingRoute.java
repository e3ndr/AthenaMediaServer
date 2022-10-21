package xyz.e3ndr.athena.routes;

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

public class StreamingRoute implements HttpProvider {

    @SneakyThrows
    @HttpEndpoint(uri = "/v1/athena/media/:mediaId/stream")
    public HttpResponse onStream(SoraHttpSession session) {
        Map<String, String> query = session.getQueryParameters();

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

        InputStream video = Athena.startStream(
            media,
            startAt,
            videoQuality, videoCodec, audioCodec, containerFormat,
            streamIds
        );

        String mimeType = String.format("video/%s; codecs=\"%s, %s\"", containerFormat.name(), videoCodec.name(), audioCodec.name()).toLowerCase();

        HttpResponse resp = HttpResponse.newFixedLengthResponse(StandardHttpStatus.OK, HttpResponse.EMPTY_BODY)
            .setMimeType(mimeType);

        ReflectionLib.setValue(resp, "content", new ResponseContent<Void>() {
            @Override
            public void write(OutputStream out) {
                try (video) {
                    byte[] buffer = new byte[Athena.STREAMING_BUFFER_SIZE];
                    int read = 0;

                    while ((read = video.read(buffer)) != -1) {
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

}
