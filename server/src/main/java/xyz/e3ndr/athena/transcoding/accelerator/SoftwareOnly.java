package xyz.e3ndr.athena.transcoding.accelerator;

import java.util.Arrays;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import lombok.NonNull;
import xyz.e3ndr.athena.types.VideoCodec;
import xyz.e3ndr.athena.types.VideoQuality;

class SoftwareOnly implements Accelerator {

    @Override
    public @Nullable List<String> acc_getFF() {
        return null;
    }

    @Override
    public @Nullable List<String> v_getFF(@NonNull VideoCodec codec, @NonNull VideoQuality quality) {
        switch (codec) {
            case SOURCE:
                return Arrays.asList(
                    "-c:v", "copy"
                );

            case H264_BASELINE:
                return Arrays.asList(
                    "-c:v", "libx264",
                    "-profile:v", "baseline",
                    "-pix_fmt", "yuv420p"
                );

            case H264_HIGH:
                return Arrays.asList(
                    "-c:v", "libx264",
                    "-profile:v", "high",
                    "-pix_fmt", "yuv420p",
                    "-level", "5.0",
                    "-tune", "film",
                    "-preset", "slow"
                );

            // TODO the more advanced parameters for HEVC and AV1
            case HEVC:
                return Arrays.asList(
                    "-c:v", "libx265"
                );

            case AV1:
                return Arrays.asList(
                    "-c:v", "libsvtav1"
                );

            case SPARK:
                return Arrays.asList(
                    "-c:v", "flv1"
                );
        }
        throw new IllegalArgumentException("Unhandled enum: " + codec);
    }

}
