package xyz.e3ndr.athena.types;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum VideoCodec {
    H264("h264", "h264_nvenc"),
    HEVC("hevc", "hevc_nvenc"),
    SORENSON_SPARK("flv1", null),
    THEORA("libtheora", null),

    ;

    private final String cpu;
    private final String cuda;

    public String getFF(boolean enableCuda) {
        if ((this.cuda != null) && enableCuda) {
            return this.cuda;
        }

        return this.cpu;
    }

}
