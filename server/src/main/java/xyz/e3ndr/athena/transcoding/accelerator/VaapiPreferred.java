package xyz.e3ndr.athena.transcoding.accelerator;

import java.util.Arrays;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import lombok.NonNull;
import xyz.e3ndr.athena.types.VideoCodec;
import xyz.e3ndr.athena.types.VideoQuality;

class VaapiPreferred implements Accelerator {
    private static final String VAAPI_DEVICE = System.getProperty("athena.vaapi.device", "/dev/dru/renderD128");

    @Override
    public @Nullable List<String> v_getFF(@NonNull VideoCodec codec, @NonNull VideoQuality quality) {
        switch (codec) {
            case H264_BASELINE:
                return Arrays.asList(
                    "-vaapi_device", VAAPI_DEVICE,
                    "-c:v", "h264_vaapi",
                    "-profile:v", "baseline",
                    "-pix_fmt", "yuv420p"
                );

            case H264_HIGH:
                return Arrays.asList(
                    "-vaapi_device", VAAPI_DEVICE,
                    "-c:v", "h264_vaapi",
                    "-profile:v", "high",
                    "-pix_fmt", "yuv420p",
                    "-level", "5.0",
                    "-preset", "slow"
                );

            // TODO the more advanced parameters for HEVC and AV1
            case HEVC:
                return Arrays.asList(
                    "-vaapi_device", VAAPI_DEVICE,
                    "-c:v", "hevc_vaapi"
                );

            case AV1:
                return Arrays.asList(
                    "-vaapi_device", VAAPI_DEVICE,
                    "-c:v", "av1_vaapi"
                );

            default:
                return null;
        }
    }

}
