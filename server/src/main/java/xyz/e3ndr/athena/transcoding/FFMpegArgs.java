package xyz.e3ndr.athena.transcoding;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import lombok.NonNull;
import xyz.e3ndr.athena.Athena;
import xyz.e3ndr.athena.transcoding.accelerator.TranscodeAcceleration;
import xyz.e3ndr.athena.types.AudioCodec;
import xyz.e3ndr.athena.types.VideoCodec;
import xyz.e3ndr.athena.types.VideoQuality;

public class FFMpegArgs {

    public static @NonNull List<String> acc_getFF() {
        List<String> args = Athena.config.transcoding.acceleration.acc_getFF();
        return args == null ? Collections.emptyList() : args;
    }

    public static @NonNull List<String> a_getFF(@NonNull AudioCodec codec) {
        switch (codec) {
            case SOURCE:
                return Arrays.asList(
                    "-c:a", "copy"
                );

            case OPUS:
                return Arrays.asList(
                    "-c:a", "libopus", // Match channel layout
                    "-b:a", "320k"
                );

            case AAC:
                return Arrays.asList(
                    "-c:a", "aac",
                    "-ac", "2",  // Limit to 2 channels.
                    "-b:a", "156k"
                );

            case MP3:
                return Arrays.asList(
                    "-c:a", "mp3",
                    "-ac", "2", // Limit to 2 channels.
                    "-b:a", "128k"
                );
        }
        throw new IllegalArgumentException("Unhandled enum: " + codec);
    }

    public static @NonNull List<String> v_getFF(@NonNull VideoCodec codec, @NonNull VideoQuality quality) {
        List<String> args = Athena.config.transcoding.acceleration.v_getFF(codec, quality);

        if (args == null) {
            // For some reason the accelerator produced no result.
            // Let's fallback to software.
            args = TranscodeAcceleration.SOFTWARE_ONLY.v_getFF(codec, quality);
        }

        return args;
    }

}
