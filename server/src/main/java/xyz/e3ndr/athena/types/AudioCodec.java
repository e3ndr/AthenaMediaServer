package xyz.e3ndr.athena.types;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum AudioCodec {
    SOURCE("copy"),

    // @formatter:off
    AAC ("aac"),
    MP3 ("mp3"),
    OPUS("libopus"),
    // @formatter:on
    ;

    public final String ff;

}
