package xyz.e3ndr.athena.types;

import java.util.Arrays;
import java.util.List;

public enum VideoCodec {
    H264,
    HEVC,
    SPARK,
    THEORA,
    VP8,
    ;

    public List<String> getFF(boolean enableCuda) {
        switch (this) {
            case H264:
                if (enableCuda) {
                    return Arrays.asList("-c:v", "h264_nvenc", "-profile:v", "main", "-level:v", "1.0");
                } else {
                    return Arrays.asList("-c:v", "h264", "-profile:v", "main", "-level:v", "1.0");
                }

            case HEVC:
                if (enableCuda) {
                    return Arrays.asList("-c:v", "hevc_nvenc");
                } else {
                    return Arrays.asList("-c:v", "hevc");
                }

            case SPARK:
                return Arrays.asList("-c:v", "flv1");

            case THEORA:
                return Arrays.asList("-c:v", "libtheora");

            case VP8:
                return Arrays.asList("-c:v", "vp8");

            default:
                return null; // Shut up compiler.
        }
    }

}
