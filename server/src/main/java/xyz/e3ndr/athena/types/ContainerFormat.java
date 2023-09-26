package xyz.e3ndr.athena.types;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import lombok.AllArgsConstructor;
import xyz.e3ndr.athena.transcoding.Transcoder;

@AllArgsConstructor
public enum ContainerFormat {
    // @formatter:off
    MKV  ("matroska", true,  Collections.emptyList()),
    MP4  ("mp4",      false, Arrays.asList("-movflags", "faststart+frag_keyframe")),
    TS   ("mpegts",   false, Collections.emptyList()),
    HLS  ("hls",      true,  Arrays.asList("-g", String.valueOf(Transcoder.HLS_RATE * Transcoder.HLS_INTERVAL), "-hls_list_size", "0", "-hls_time", String.valueOf(Transcoder.HLS_INTERVAL))),
    // @formatter:on

    // Needs more testing but a lot of browsers accidentally support Matroska since
    // Webm is based on it. Needs more testing by it could be a good way to give the
    // browser more metadata. (It works because the headers for Webm & Matroska are
    // the same, and the browser will actually do a content probe instead of just
    // trusting the mime type. This is an easy way of getting all the benefits of
    // Webm whilst still supporting H264 and HEVC)

    ;

    public final String ff;
    public final boolean streamable;
    public final List<String> flags;

}
