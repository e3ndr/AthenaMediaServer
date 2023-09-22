package xyz.e3ndr.athena.types;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum VideoQuality {
    // @formatter:off
    UHD(2160, 10000),
    FHD(1080, 4000),
    HD (720,  2500),
    SD (480,  1000),
    LD (240,  450),
    // @formatter:on

    ;

    public final int max;

    /** KBPS */
    public final int bitrate;

}
