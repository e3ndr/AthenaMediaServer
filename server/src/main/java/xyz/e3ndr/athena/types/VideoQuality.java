package xyz.e3ndr.athena.types;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum VideoQuality {
    // @formatter:off
    UHD(2160, 25000),
    FHD(1080,  7500),
    HD (720,   4000),
    SD (480,   1500),
    LD (240,    500),
    // @formatter:on

    ;

    public final int max;

    /** KBPS */
    public final int bitrate;

}
