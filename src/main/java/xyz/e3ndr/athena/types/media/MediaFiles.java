package xyz.e3ndr.athena.types.media;

import java.util.ArrayList;
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

        private List<VideoStream> video;
        private List<AudioStream> audio;

        public List<Stream> getAll() {
            List<Stream> all = new ArrayList<>(this.video.size() + this.audio.size());
            all.addAll(this.video);
            all.addAll(this.audio);
            return all;
        }

        @Getter
        @NonNull
        @JsonClass(exposeAll = true)
        public static class VideoStream extends Stream {
            private int frameRate;
            private int width;
            private int height;

        }

        @Getter
        @NonNull
        @JsonClass(exposeAll = true)
        public static class AudioStream extends Stream {
            private String language;
            private int channels;

        }

        @Getter
        @NonNull
        @JsonClass(exposeAll = true)
        public static abstract class Stream {
            private int id;
            private String name;
            private String codec;

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
