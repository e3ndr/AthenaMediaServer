package xyz.e3ndr.athena;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ProcessBuilder.Redirect;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.rakurai.io.IOUtil;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonArray;
import co.casterlabs.rakurai.json.element.JsonObject;
import xyz.e3ndr.athena.transcoding.TranscodeSession;
import xyz.e3ndr.athena.transcoding.Transcoder;
import xyz.e3ndr.athena.types.AudioCodec;
import xyz.e3ndr.athena.types.ContainerFormat;
import xyz.e3ndr.athena.types.VideoCodec;
import xyz.e3ndr.athena.types.VideoQuality;
import xyz.e3ndr.athena.types.media.Media;
import xyz.e3ndr.athena.types.media.MediaFiles.Streams.Stream;

public class Athena {
    public static final String STREAM_FORMAT = "mkv";
    public static final String STREAM_FORMAT_FF = "matroska";

    public static final int STREAMING_BUFFER_SIZE = 64/*kb*/ * 1000;
    public static final int TRANSCODING_BUFFER_SIZE = 512/*kb*/ * 1000;

    public static File mediaDirectory;
    public static File cacheDirectory;
    public static File ingestDirectory;

    public static String omdbApiKey;

    public static boolean enableCudaAcceleration;

    public static List<MediaSession> mediaSessions = Collections.synchronizedList(new LinkedList<>());
    public static List<TranscodeSession> transcodeSessions = Collections.synchronizedList(new LinkedList<>());

    /* -------------------- */
    /* Auth                 */
    /* -------------------- */

    public static boolean authenticate(String token) {
        return true; // TODO
    }

    /* -------------------- */
    /* Ingesting            */
    /* -------------------- */

    public static void ingest(String fileName, Media media) throws IOException, InterruptedException {
        File mediaFile = new File(ingestDirectory, fileName);

        File mediaIndexLocation = new File(mediaDirectory, media.getId());
        File mediaStreamsLocation = new File(mediaIndexLocation, "streams");
        File mediaSubtitlesLocation = new File(mediaIndexLocation, "subtitles");

        mediaIndexLocation.mkdirs();
        mediaStreamsLocation.mkdirs();
        mediaSubtitlesLocation.mkdirs();

        Files.write(
            new File(mediaIndexLocation, "index.json").toPath(),
            Rson.DEFAULT
                .toJson(media)
                .toString(true)
                .getBytes(StandardCharsets.UTF_8)
        );

        List<Process> processes = new LinkedList<>();

        // Start them all simultaneously.
        for (Stream stream : media.getFiles().getStreams().getAll()) {
            String streamId = String.valueOf(stream.getId());
            File streamFile = new File(mediaStreamsLocation, streamId.concat(".mkv"));

            Process process = new ProcessBuilder()
                .command(
                    "ffmpeg",
                    "-hide_banner",
                    "-v", "error",
                    "-i", mediaFile.getCanonicalPath(),
                    "-map", "0:".concat(streamId),
                    "-c", "copy",
                    "-f", "matroska",
                    streamFile.getCanonicalPath()
                )
                .redirectInput(Redirect.PIPE)
                .redirectError(Redirect.INHERIT)
                .redirectOutput(Redirect.DISCARD)
                .start();

            processes.add(process);
        }

        // Wait for them to complete.
        for (Process process : processes) {
            process.waitFor();
        }
    }

    public static void streamIngestable(String fileName, int streamId, OutputStream target) {
        try {
            File mediaFile = new File(ingestDirectory, fileName);

            List<String> command = new LinkedList<>();
            command.add("ffmpeg");
            command.add("-hide_banner");
            command.add("-v");
            command.add("error");

            command.add("-i");
            command.add(mediaFile.getCanonicalPath());

            command.add("-map");
            command.add("0:".concat(String.valueOf(streamId)));

            command.add("-c");
            command.add("copy");

            /* ---- Format & Output ---- */
            command.add("-bufsize");
            command.add(String.valueOf(Athena.STREAMING_BUFFER_SIZE));

            command.addAll(ContainerFormat.MKV.flags);
            command.add("-f");
            command.add(ContainerFormat.MKV.ff);
            command.add("pipe:1");

            final Process proc = new ProcessBuilder()
                .command(command)
                .redirectInput(Redirect.PIPE)
                .redirectError(Redirect.INHERIT)
                .redirectOutput(Redirect.PIPE)
                .start();

            try {
                IOUtil.writeInputStreamToOutputStream(proc.getInputStream(), target, Athena.STREAMING_BUFFER_SIZE);
            } finally {
                proc.destroy();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static JsonArray getIngestableInfo(String fileName) {
        try {
            File mediaFile = new File(ingestDirectory, fileName);
            InputStream mediaFileStreams = new ProcessBuilder()
                .command(
                    "ffprobe",
                    "-hide_banner",
                    "-v", "error",
                    "-show_streams",
                    "-of", "json",
                    mediaFile.getCanonicalPath()
                )
                .start()
                .getInputStream();
            String mediaFileString = IOUtil.readInputStreamString(mediaFileStreams, StandardCharsets.UTF_8);

            return Rson.DEFAULT
                .fromJson(mediaFileString, JsonObject.class)
                .getArray("streams");
        } catch (Exception e) {
//            e.printStackTrace();
            return null;
        }
    }

    public static List<String> listIngestables() {
        return Arrays.asList(ingestDirectory.list());
    }

    /* -------------------- */
    /* Media                */
    /* -------------------- */

    public static MediaSession startStream(Media media, VideoQuality desiredQuality, VideoCodec desiredVCodec, AudioCodec desiredACodec, ContainerFormat desiredContainer, int... streamIds) throws IOException {
        final File cacheFile = Transcoder.getFile(media, desiredQuality, desiredVCodec, desiredACodec, desiredContainer, streamIds);
        TranscodeSession transcodeSession = null;

        if (!cacheFile.exists()) {
            transcodeSession = Transcoder.start(cacheFile, media, desiredQuality, desiredVCodec, desiredACodec, desiredContainer, streamIds);
        }

        return new MediaSession(cacheFile, transcodeSession, media.getId(), desiredQuality, desiredVCodec, desiredACodec, desiredContainer, streamIds);
    }

    public static @Nullable Media getMedia(String mediaId) {
        try {
            File mediaIndexFile = new File(mediaDirectory, mediaId.concat("/index.json"));
            String mediaIndex = Files.readString(mediaIndexFile.toPath(), StandardCharsets.UTF_8);

            return Rson.DEFAULT.fromJson(mediaIndex, Media.class);
        } catch (Exception ignored) {
            return null;
        }
    }

    public static List<Media> listMedia() {
        return Arrays.asList(mediaDirectory.list())
            .parallelStream()
            .map(Athena::getMedia)
            .filter((mediaId) -> mediaId != null) // Remove empty results.
            .collect(Collectors.toList());
    }

    public static List<Media> searchMedia(String query) {
        query = query
            .toLowerCase()
            .trim();

        if (query.length() == 0) {
            return listMedia();
        }

        String $_query = query;
        return Arrays.asList(mediaDirectory.list())
            .parallelStream()
            .map(Athena::getMedia)
            .filter((mediaId) -> mediaId != null) // Remove empty results.
            .filter((media) -> {
                // TODO a better search.
                return media.getInfo().getTitle().toLowerCase().contains($_query) ||
                    media.getInfo().getSummary().toLowerCase().contains($_query);
            }
            )
            .collect(Collectors.toList());
    }

}
