package xyz.e3ndr.athena.types;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum ContainerFormat {
    // @formatter:off
    MP4  ("mp4",      Arrays.asList("-movflags", "faststart+frag_keyframe")),
    SWF  ("swf",      Collections.emptyList()),
    FLV  ("flv",      Collections.emptyList()),
    OGG  ("ogg",      Collections.emptyList()),
    WEBM ("webm",     Collections.emptyList()),
    MKV  ("matroska", Collections.emptyList()),
    TS   ("mpegts",   Collections.emptyList()),
    // @formatter:on

    // Needs more testing but a lot of browsers accidentally support Matroska since
    // Webm is based on it. Needs more testing by it could be a good way to give the
    // browser more metadata. (It works because the headers for Webm & Matroska are
    // the same, and the browser will actually do a content probe instead of just
    // trusting the mime type. This is an easy way of getting all the benefits of
    // Webm whilst still supporting H264 and HEVC)

    ;

    public final String ff;
    public final List<String> flags;

}
