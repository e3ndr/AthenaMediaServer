package xyz.e3ndr.athena;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ProcessBuilder.Redirect;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

import co.casterlabs.commons.async.AsyncTask;
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

    private static Map<String, PlaybackSession> sessions = new ConcurrentHashMap<>();

    public List<PlaybackSession> getSessions() {
        return new ArrayList<>(sessions.values());
    }

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
            command.add(String.format("scale='min(%d,iw)':-1", desiredQuality.max));
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
            .redirectError(Redirect.PIPE)
            .redirectInput(Redirect.PIPE)
            .redirectOutput(Redirect.PIPE)
            .start();

        /* ---- Session & Analytics ---- */
        PlaybackSession session = new PlaybackSession();
        sessions.put(session.id, session);
        session.logger.info("Session started.");

        proc
            .onExit()
            .whenComplete((_1, _2) -> {
                sessions.remove(session.id);
                session.logger.info("Session ended.");
            });

        {
            AsyncTask.create(() -> {
                Scanner stdout = new Scanner(proc.getErrorStream());

                try {
                    String line = null;
                    while ((line = stdout.nextLine()) != null) {
                        if (line.startsWith("frame=")) {
                            session.parseAndUpdate(line);
                            session.logger.info("Analytics: %s", session);
                        }

                        session.logger.debug(line);
                    }
                } catch (NoSuchElementException ignored) {}
            });
        }

        /* ---- Result ---- */
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

    public static boolean authenticate(String token) {
        return true; // TODO
    }

    public static List<String> listMedia() {
        List<String> result = new LinkedList<>();

        for (String mediaId : mediaDirectory.list()) {
            try {
                Media media = getMedia(mediaId);
                result.add(media.toString());
            } catch (IOException ignored) {}
        }

        return result;
    }

}
