package xyz.e3ndr.athena;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ProcessBuilder.Redirect;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;

import co.casterlabs.rakurai.json.Rson;
import xyz.e3ndr.athena.types.AudioCodec;
import xyz.e3ndr.athena.types.ContainerFormat;
import xyz.e3ndr.athena.types.VideoCodec;
import xyz.e3ndr.athena.types.VideoQuality;
import xyz.e3ndr.athena.types.media.Media;

public class Athena {
    public static final String STREAM_FORMAT = "mkv";
    public static final String STREAM_FORMAT_FF = "matroska";

    public static final int STREAMING_BUFFER_SIZE = 64/*kb*/ * 1000;

    public static File mediaDirectory;

    public static boolean enableCudaAcceleration;

    public static Media getMedia(String mediaId) throws IOException {
        File mediaIndexFile = new File(mediaDirectory, mediaId.concat("/index.json"));
        String mediaIndex = Files.readString(mediaIndexFile.toPath(), StandardCharsets.UTF_8);

        return Rson.DEFAULT.fromJson(mediaIndex, Media.class);
    }

    public static InputStream startStream(Media media, long startAt, VideoQuality desiredQuality, VideoCodec desiredVCodec, AudioCodec desiredACodec, ContainerFormat desiredContainer, int... streamIds) throws IOException {
        List<String> command = new LinkedList<>();
        command.add("ffmpeg");
        command.add("-hide_banner");
//      command.add("-v");
//      command.add("error");

        /* ---- Streams/Input ---- */
        for (int streamId : streamIds) {
//            command.add("-re");

            // We need to add -ss before every input.
            // And we want them to line up.
            command.add("-ss");
            command.add(String.format("%dms", startAt));

            command.add("-i");
            command.add(media.getStreamFile(streamId).getCanonicalPath());
        }

        if (desiredQuality == VideoQuality.SOURCE) {
            // Just copy the codecs.
            command.add("-c");
            command.add("copy");
        } else {
            /* ---- Audio ---- */
            command.add("-c:a");
            command.add(desiredACodec.ff);

            /* ---- Video ---- */
            command.add("-c:v");
            command.add(desiredVCodec.getFF(Athena.enableCudaAcceleration));

            if (!enableCudaAcceleration) {
                command.add("-tune");
                command.add("zerolatency");
            }

            if (desiredContainer == ContainerFormat.FLV) {
                command.add("-maxrate");
                command.add(String.format("%dK", desiredQuality.bitrate));
            } else {
                command.add("-b:v");
                command.add(String.format("%dK", desiredQuality.bitrate));
            }

            // https://trac.ffmpeg.org/wiki/Scaling
            command.add("-vf");
            command.add(String.format("scale='min(%d,iw)':'min(%d,ih)'", desiredQuality.max, desiredQuality.max));
        }

        /* ---- Format & Output ---- */
        command.add("-bufsize");
        command.add(String.valueOf(STREAMING_BUFFER_SIZE));

        command.addAll(desiredContainer.flags);
        command.add("-f");
        command.add(desiredContainer.ff);
        command.add("pipe:1");

        Process proc = new ProcessBuilder()
            .command(command)
            .redirectError(Redirect.INHERIT)
            .redirectInput(Redirect.PIPE)
            .redirectOutput(Redirect.PIPE)
            .start();

        InputStream videoStream = proc.getInputStream();

        return new InputStream() {

            @Override
            public int read() throws IOException {
                return videoStream.read();
            }

            @Override
            public int read(byte[] b) throws IOException {
                return videoStream.read(b);
            }

            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                return videoStream.read(b, off, len);
            }

            @Override
            public void close() throws IOException {
                proc.destroy();
            }

        };
    }

}
