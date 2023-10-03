package xyz.e3ndr.athena.transcoding.accelerator;

import java.util.Arrays;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import lombok.NonNull;
import xyz.e3ndr.athena.types.VideoCodec;
import xyz.e3ndr.athena.types.VideoQuality;

class NvidiaPreferred implements Accelerator {

    @Override
    public @Nullable List<String> v_getFF(@NonNull VideoCodec codec, @NonNull VideoQuality quality) {
        switch (codec) {
            case H264_BASELINE:
                return Arrays.asList(
                    "-c:v", "h264_nvenc",
                    "-profile:v", "baseline",
                    "-pix_fmt", "yuv420p"
                );

            case H264_HIGH:
                return Arrays.asList(
                    "-c:v", "h264_nvenc",
                    "-profile:v", "high",
                    "-pix_fmt", "yuv420p",
                    "-level", "5.0",
                    "-preset", "slow"
                );

            // TODO the more advanced parameters for HEVC and AV1
            case HEVC:
                return Arrays.asList(
                    "-c:v", "hevc_nvenc"
                );

            case AV1:
                return Arrays.asList(
                    "-c:v", "av1_nvenc"
                );

            default:
                return null;
        }
    }

}
