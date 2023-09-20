package xyz.e3ndr.athena.transcoding;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ProcessBuilder.Redirect;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

import co.casterlabs.commons.async.AsyncTask;
import co.casterlabs.commons.async.PromiseWithHandles;
import lombok.SneakyThrows;
import xyz.e3ndr.athena.Athena;
import xyz.e3ndr.athena.types.AudioCodec;
import xyz.e3ndr.athena.types.ContainerFormat;
import xyz.e3ndr.athena.types.VideoCodec;
import xyz.e3ndr.athena.types.VideoQuality;
import xyz.e3ndr.athena.types.media.Media;

public class Transcoder {

    @SneakyThrows
    public static TranscodeSession start(File targetFile, Media media, VideoQuality desiredQuality, VideoCodec desiredVCodec, AudioCodec desiredACodec, ContainerFormat desiredContainer, int... streamIds) {
        List<String> command = new LinkedList<>();
        command.add("ffmpeg");
        command.add("-hide_banner");
//      command.add("-v");
//      command.add("error");

        /* ---- Streams/Input ---- */
        for (int streamId : streamIds) {
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
            command.addAll(desiredVCodec.getFF(Athena.enableCudaAcceleration));

            if (desiredContainer == ContainerFormat.FLV || desiredContainer == ContainerFormat.SWF) {
                command.add("-maxrate");
                command.add(String.format("%dK", desiredQuality.bitrate));
                command.add("-ar");
                command.add("44100");
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
        command.add(String.valueOf(Athena.TRANSCODING_BUFFER_SIZE));

        command.addAll(desiredContainer.flags);
        command.add("-f");
        command.add(desiredContainer.ff);
        command.add("pipe:1");

        final Process proc = new ProcessBuilder()
            .command(command)
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
                    session.logger.debug(line);

                    if (line.startsWith("frame=")) {
                        if (!hasStarted) {
                            session.init(initInfoBuilder);
                            initInfoBuilder = null;
                            hasStarted = true;
                            session.logger.debug("Started!");
                            startPromise.resolve(null);
                        }

                        session.processStatistic(line);
                        session.logger.debug(session);
                    } else if (initInfoBuilder != null) {
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

                session.setComplete(true);
            }
        });

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

        startPromise.await(); // Wait for the session to start.

        return session;
    }

    public static File getFile(Media media, VideoQuality desiredQuality, VideoCodec desiredVCodec, AudioCodec desiredACodec, ContainerFormat desiredContainer, int... streamIds) {
        List<String> str_streamIds = new ArrayList<>(streamIds.length);
        for (int streamId : streamIds) {
            str_streamIds.add(String.valueOf(streamId));
        }

        List<String> codecs = new ArrayList<>();
        codecs.add(desiredQuality == VideoQuality.SOURCE ? "source" : desiredVCodec.name().toLowerCase());
        codecs.add(desiredACodec.name().toLowerCase());

        return new File(
            Athena.cacheDirectory,
            String.format(
                "%s/%s/%s/%s.%s",
                media.getId(),
                desiredQuality.name().toLowerCase(),
                String.join(",", codecs),
                String.join(",", str_streamIds),
                desiredContainer.name().toLowerCase()
            )
        );
    }

}
