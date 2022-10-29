package xyz.e3ndr.athena.types.media;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.rakurai.json.annotating.JsonClass;
import lombok.Getter;
import lombok.NonNull;

@Getter
@NonNull
@JsonClass(exposeAll = true)
public class MediaFiles {
    private MediaFiles.Images images;
    private MediaFiles.Streams streams;
    private List<MediaFiles.Subtitle> subtitles;

    @Getter
    @JsonClass(exposeAll = true)
    public static class Images {
        private @Nullable String posterUrl;

    }

    @Getter
    @NonNull
    @JsonClass(exposeAll = true)
    public static class Streams {
        private int[] defaultStreams;

        private List<Video> video;
        private List<Audio> audio;

        @Getter
        @NonNull
        @JsonClass(exposeAll = true)
        public static class Video {
            private int id;
            private String codec;
            private String quality;
            private int frameRate;
            private int width;
            private int height;

        }

        @Getter
        @NonNull
        @JsonClass(exposeAll = true)
        public static class Audio {
            private String name;
            private int id;
            private String codec;
            private String language;
            private int channels;

        }

    }

    @Getter
    @NonNull
    @JsonClass(exposeAll = true)
    public static class Subtitle {
        private String language;
        private String file;
        private List<String> forced;

    }

}
