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
    private @NonNull List<String> studios;

    private @NonNull Ratings ratings;

    @Getter
    @NonNull
    @JsonClass(exposeAll = true)
    public static class Director {
        private String id;
        private String name;
        private String imageUrl;
    }

    @Getter
    @NonNull
    @JsonClass(exposeAll = true)
    public static class Actor {
        private String id;
        private String name;
        private String playedCharacter;
        private String imageUrl;
    }

    @Getter
    @NonNull
    @JsonClass(exposeAll = true)
    public static class Studio {
        private String id;
        private String name;
        private String logoUrl;
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
