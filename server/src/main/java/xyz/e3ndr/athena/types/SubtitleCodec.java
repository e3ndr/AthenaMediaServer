package xyz.e3ndr.athena.types;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum SubtitleCodec {
    SOURCE("copy"),

    // @formatter:off
    WEBVTT ("webvtt"),
    SRT    ("srt"),
    ASS    ("ass"),
    // @formatter:on
    ;

    public final String ff;

}
