package xyz.e3ndr.athena.types.media;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.annotating.JsonField;
import lombok.Getter;
import lombok.NonNull;

@Getter
@JsonClass(exposeAll = true)
public class MediaInfo {
    private @NonNull String title;
    private @Nullable String summary;
    private @Nullable String audienceRating; // null = unrated
    private @NonNull List<String> genres;

    private int day = -1;
    private int month = -1;
    private int year = -1;

    private @NonNull List<Director> directors;
    private @NonNull List<Actor> actors;
    private @NonNull List<Studio> studios;

    private @NonNull Ratings ratings;

    @Getter
    @JsonClass(exposeAll = true)
    public static class Director {
        private @NonNull String id;
        private @NonNull String name;
        private @Nullable String imageUrl;
    }

    @Getter

    @JsonClass(exposeAll = true)
    public static class Actor {
        private @NonNull String id;
        private @NonNull String name;
        private @NonNull String playedCharacter;
        private @Nullable String imageUrl;
    }

    @Getter
    @NonNull
    @JsonClass(exposeAll = true)
    public static class Studio {
        private @NonNull String id;
        private @NonNull String name;
        private @Nullable String logoUrl;
    }

    @Getter
    @JsonClass(exposeAll = true)
    public static class Ratings {

        @Nullable
        @JsonField("IMDB")
        private String imdbRating;

        @Nullable
        @JsonField("ROTTEN_TOMATOES")
        private String rottentomatoesRating;

        @Nullable
        @JsonField("METACRITIC")
        private String metacriticRating;

    }

}
