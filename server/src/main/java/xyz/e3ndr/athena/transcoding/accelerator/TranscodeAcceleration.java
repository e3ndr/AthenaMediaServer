package xyz.e3ndr.athena.transcoding.accelerator;

import java.lang.ProcessBuilder.Redirect;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import org.jetbrains.annotations.Nullable;

import lombok.NonNull;
import xyz.e3ndr.athena.transcoding.CommandBuilder;
import xyz.e3ndr.athena.transcoding.Transcoder;
import xyz.e3ndr.athena.types.VideoCodec;
import xyz.e3ndr.athena.types.VideoQuality;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

public enum TranscodeAcceleration {
    SOFTWARE_ONLY(new SoftwareOnly()),
    NVIDIA_PREFERRED(new NvidiaPreferred()),
    // TODO AMD & Intel encoders.
    // TODO Implement hardware decoding to speed up the transcode pipeline.
    // https://trac.ffmpeg.org/wiki/HWAccelIntro
    ;

    // Forward to the actual IMPL.
    private final Accelerator instance;
    private final List<VideoCodec> supported = new LinkedList<>();

    private TranscodeAcceleration(@NonNull Accelerator instance) {
        this.instance = instance;
    }

    public @Nullable List<String> acc_getFF() {
        return this.instance.acc_getFF();
    }

    public @Nullable List<String> v_getFF(@NonNull VideoCodec codec, @NonNull VideoQuality quality) {
        if (this.supported.contains(codec)) {
            return this.instance.v_getFF(codec, quality);
        } else {
            return null; // Not supported!
        }
    }

    public static void runTests() {
        for (TranscodeAcceleration a : values()) {
            for (VideoCodec codec : VideoCodec.values()) {
                a.tryCodec(codec);
            }
        }
    }

    private void tryCodec(VideoCodec codec) {
        FastLogger.logStatic(LogLevel.INFO, "[Acceleration=%s] Detecting support for VideoCodec %s...", this.name(), codec);

        CommandBuilder command = new CommandBuilder();
        command.add(Transcoder.FFMPEG_EXEC);
        command.add("-hide_banner");
        command.add("-v", "warning");

        command.add("-f", "lavfi");
        command.add("-i", "testsrc=duration=1:size=1280x720:rate=30");

        List<String> vCodecArgs = this.instance.v_getFF(codec, VideoQuality.FHD);
        if (vCodecArgs == null) {
            FastLogger.logStatic(LogLevel.DEBUG, "[Acceleration=%s] VideoCodec %s is NOT supported! (No args) This is probably fine...", this.name(), codec);
            return;
        }
        command.add(vCodecArgs);

        command.add("-f", "null");
        command.add("pipe:1");

        try {
            final Process proc = new ProcessBuilder()
                .command(command.asList())
                .redirectError(Redirect.PIPE)
                .redirectInput(Redirect.PIPE)
                .redirectOutput(Redirect.DISCARD)
                .start();

            if (proc.waitFor() == 0) {
                FastLogger.logStatic(LogLevel.INFO, "[Acceleration=%s] Success! VideoCodec %s is supported!", this.name(), codec);
                this.supported.add(codec);
            } else {
                FastLogger.logStatic(LogLevel.WARNING, "[Acceleration=%s] VideoCodec %s is NOT supported! This is probably fine...", this.name(), codec);

                Scanner output = new Scanner(proc.getErrorStream());
                while (output.hasNext()) {
                    FastLogger.logStatic(LogLevel.DEBUG, "[Acceleration=%s] %s", this.name(), output.nextLine());
                }
            }
        } catch (Exception e) {
            FastLogger.logStatic(LogLevel.SEVERE, "[Acceleration=%s] An error occurred whilst detecting support for VideoCodec %s.\n%s", this.name(), codec, e);
        }
    }

}
