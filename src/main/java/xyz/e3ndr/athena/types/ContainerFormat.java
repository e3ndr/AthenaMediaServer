package xyz.e3ndr.athena.types;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum ContainerFormat {
    // @formatter:off
    MP4("mp4", Arrays.asList("-movflags", "faststart+frag_keyframe+empty_moov")),
    FLV("flv", Collections.emptyList()),
    OGG("ogg", Collections.emptyList()),
    // @formatter:on

    ;

    public final String ff;
    public final List<String> flags;

}
