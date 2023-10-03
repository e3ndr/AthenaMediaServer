package xyz.e3ndr.athena.transcoding;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ProcessBuilder.Redirect;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.commons.async.AsyncTask;
import co.casterlabs.commons.async.PromiseWithHandles;
import co.casterlabs.rakurai.io.IOUtil;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.SneakyThrows;
import xyz.e3ndr.athena.Athena;
import xyz.e3ndr.athena.types.AudioCodec;
import xyz.e3ndr.athena.types.ContainerFormat;
import xyz.e3ndr.athena.types.VideoCodec;
import xyz.e3ndr.athena.types.VideoQuality;
import xyz.e3ndr.athena.types.media.Media;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

public class Transcoder {
    public static final String FFMPEG_EXEC = System.getProperty("athena.ffmpeg", "ffmpeg");
    public static final String FFPROBE_EXEC = System.getProperty("athena.ffprobe", "ffprobe");

    public static final int HLS_RATE = 25;
    public static final int HLS_INTERVAL = 10;

    private static final FastLogger logger = new FastLogger();

    @SneakyThrows
    public static @Nullable TranscodeSession start(File targetFile, Media media, VideoQuality desiredQuality, VideoCodec desiredVCodec, AudioCodec desiredACodec, ContainerFormat desiredContainer, int... streamIds) {
        if (!Athena.config.transcoding.enable && (desiredACodec != AudioCodec.SOURCE || desiredVCodec != VideoCodec.SOURCE)) {
            logger.severe("Transcoding is disabled, but a session was requested.");
            return null;
        }

        CommandBuilder command = new CommandBuilder();
        command.add(FFMPEG_EXEC);
        command.add("-hide_banner");
//      command.add("-v", "error");

        command.add(FFMpegArgs.acc_getFF());

        /* ---- Streams/Input ---- */
        for (int streamId : streamIds) {
            command.add("-i", media.getStreamFile(streamId).getCanonicalPath());
        }

        /* ---- Audio ---- */
        command.add(FFMpegArgs.a_getFF(desiredACodec));

        if (desiredACodec != AudioCodec.SOURCE) {
            command.add("-ar", "48000");
        }

        /* ---- Video ---- */
        command.add(FFMpegArgs.v_getFF(desiredVCodec, desiredQuality));

        if (desiredVCodec != VideoCodec.SOURCE) {
            command.add("-b:v", String.format("%dK", desiredQuality.bitrate));

            // https://trac.ffmpeg.org/wiki/Scaling
            command.add("-vf", String.format("scale='min(%d,iw)':-1, pad=ceil(iw/2)*2:ceil(ih/2)*2", desiredQuality.max));
        }

        /* ---- Format & Output ---- */
        command.add("-bufsize", String.valueOf(Athena.TRANSCODING_BUFFER_SIZE));

        command.add(desiredContainer.flags);

        if (desiredContainer == ContainerFormat.HLS) {
            targetFile.mkdir(); // Make it a directory.
            command.add("-vf", "fps=fps=" + HLS_RATE);
            command.add(new File(targetFile, "media.m3u8").getCanonicalPath());
        } else {
            command.add("-f", desiredContainer.ff);
            command.add("pipe:1");
        }

        final Process proc = new ProcessBuilder()
            .command(command.asList())
            .redirectError(Redirect.PIPE)
            .redirectInput(Redirect.PIPE)
            .redirectOutput(Redirect.PIPE)
            .start();

        /* ---- Session & Analytics ---- */
        TranscodeSession session = new TranscodeSession(media.getId(), targetFile, desiredQuality, desiredVCodec, desiredACodec, desiredContainer, streamIds);
        PromiseWithHandles<Void> startPromise = new PromiseWithHandles<>();

        Athena.transcodeSessions.add(session);

        AsyncTask.create(() -> {
            Scanner stdout = new Scanner(proc.getErrorStream());
            boolean hasStarted = false;

            try {
                List<String> initInfoBuilder = new LinkedList<>();

                String line = null;
                while ((line = stdout.nextLine()) != null) {
                    session.logger.trace(line);

                    if (line.startsWith("frame=")) {
                        if (!hasStarted) {
                            session.init(initInfoBuilder);
                            initInfoBuilder = null;
                            hasStarted = true;
                            session.logger.debug("Started!");
                            session.logger.debug(initInfoBuilder);
                            startPromise.resolve(null);
                        }

                        session.processStatistic(line);
                        session.logger.debug(session);
                    } else if (initInfoBuilder != null) {
                        session.logger.debug(line);
                        // This gets set to null after the video starts.
                        initInfoBuilder.add(line);
                    }
                }
            } catch (NoSuchElementException ignored) {
                // Ignored.
            } finally {
                if (!hasStarted) {
                    startPromise.resolve(null); // Just in case...
                }
            }
        });

        if (desiredContainer == ContainerFormat.HLS) {
            // Let FFMPEG handle everything from here.
            // We have to....
            AsyncTask.create(() -> {
                try {
                    proc.waitFor();
                } catch (InterruptedException ignored) {} finally {
                    Athena.transcodeSessions.remove(session);
                    session.logger.debug("Stopped transcode.");
                    proc.destroy();
                }
            });
        } else {
            // Create the file.
            targetFile.getParentFile().mkdirs();

            // FFMpeg's default implementation flushes inconsistently, so we use our own.
            AsyncTask.create(() -> {
                try (FileOutputStream fos = new FileOutputStream(targetFile)) {
                    InputStream pipe = proc.getInputStream();

                    byte[] buffer = new byte[Athena.TRANSCODING_BUFFER_SIZE];
                    int read = 0;

                    while ((read = pipe.read(buffer)) != -1) {
                        fos.write(buffer, 0, read);
                        fos.flush();
                    }
                } catch (IOException e) {
                    session.logger.exception(e);
                } finally {
                    Athena.transcodeSessions.remove(session);
                    session.logger.debug("Stopped transcode.");
                    proc.destroy();
                }
            });
        }

        startPromise.await(); // Wait for the session to start.

        return session;
    }

    @SneakyThrows
    public static File getFile(Media media, VideoQuality desiredQuality, VideoCodec desiredVCodec, AudioCodec desiredACodec, ContainerFormat desiredContainer, int... streamIds) {
        List<String> str_streamIds = new ArrayList<>(streamIds.length);
        for (int streamId : streamIds) {
            str_streamIds.add(String.valueOf(streamId));
        }

        List<String> codecs = new ArrayList<>();
        codecs.add(desiredVCodec.name().toLowerCase());
        codecs.add(desiredACodec.name().toLowerCase());

        File mediaFile = new File(
            Athena.cacheDirectory,
            String.format(
                "%s.%s.%s.%s.%s",
                media.getId(),
                desiredQuality.name().toLowerCase(),
                String.join(",", codecs),
                String.join(",", str_streamIds),
                desiredContainer.name().toLowerCase()
            )
        );
        File lastAccessedFile = new File(
            Athena.cacheDirectory,
            String.format(
                "%s.%s.%s.%s.%s.lastaccess",
                media.getId(),
                desiredQuality.name().toLowerCase(),
                String.join(",", codecs),
                String.join(",", str_streamIds),
                desiredContainer.name().toLowerCase()
            )
        );

        // Write the last access time to disk, replacing any existing timestamp.
        Files.write(
            lastAccessedFile.toPath(),
            String.valueOf(System.currentTimeMillis()).getBytes(),
            StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING
        );

        return mediaFile;
    }

    public static long getMediaLength(Media media, VideoQuality desiredQuality, VideoCodec desiredVCodec, AudioCodec desiredACodec, ContainerFormat desiredContainer, int... streamIds) {
        try {
            List<String> command = new LinkedList<>();
            command.add(FFPROBE_EXEC);
            command.add("-hide_banner");
            command.add("-v");
            command.add("error");
            command.add("-show_entries");
            command.add("format=duration");
            command.add("-of");
            command.add("json");

            command.add("-i");
            command.add(media.getStreamFile(streamIds[0]).getCanonicalPath()); // TODO TEMP, need to find the longer file.

            Process proc = new ProcessBuilder()
                .command(command)
                .redirectError(Redirect.PIPE)
                .redirectInput(Redirect.PIPE)
                .redirectOutput(Redirect.PIPE)
                .start();
            String json = IOUtil.readInputStreamString(proc.getInputStream(), StandardCharsets.UTF_8);

            JsonObject result = Rson.DEFAULT.fromJson(json, JsonObject.class);
            JsonObject format = result.getObject("format");

            double duration = Double.parseDouble(format.getString("duration")); // "123.123" in seconds.

            return (long) (duration * 1000);
        } catch (Exception ignored) {
            return 0;
        }
    }

}
