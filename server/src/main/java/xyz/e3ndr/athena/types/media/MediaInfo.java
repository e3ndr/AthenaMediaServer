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

    private int day = -1;
    private int month = -1;
    private int year = -1;

    private @Nullable String summary;
    private @Nullable String rating; // null = unrated

    private @NonNull MediaInfo.People people;

    private @NonNull List<String> genres;
    private @NonNull List<String> studios;

    private @NonNull Ratings ratings;

    @Getter
    @NonNull
    @JsonClass(exposeAll = true)
    public static class People {
        private List<String> directors;
        private List<String> writers;
        private List<String> actors;
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
