package xyz.e3ndr.athena.types;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum AudioCodec {
    SOURCE("copy"),

    AAC("aac"),
    MP3("mp3"),
    OPUS("libopus"),

    ;

    public final String ff;

}
