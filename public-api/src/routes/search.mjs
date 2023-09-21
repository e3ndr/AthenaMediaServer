import * as env from "../env.mjs";
import { JsonResponse } from "./index.mjs";

const TMDB_IMAGE_BASE = "https://image.tmdb.org/t/p/original";

export default (/** @type {import("itty-router").RouterType<import("itty-router").Route, any[]} */router) => {

    router.get("/search", async (request) => {
        const query = new URL(request.url).searchParams.get("query");

        const searchResult = await fetch(`https://api.themoviedb.org/3/search/movie?query=${encodeURIComponent(query)}&include_adult=true&page=1&api_key=${encodeURIComponent(env.TMDB_API_KEY)}`)
            .then((response) => response.json());

        const results = [];

        for (const { id: movieId } of searchResult.results) {
            results.push(
                await fetch(`https://api.themoviedb.org/3/movie/${encodeURI(movieId)}?append_to_response=image,credits&api_key=${encodeURIComponent(env.TMDB_API_KEY)}`)
                    .then((response) => response.json())
                    .then((movieData) => {
                        const [releaseYear, releaseMonth, releaseDay] = movieData.release_date.split("-");

                        return {
                            type: "MOVIE",
                            id: `TMDB_${movieId}`,
                            info: {
                                title: movieData.title,
                                audienceRating: null, // TODO, unrated for now.
                                summary: movieData.overview,
                                genres: movieData.genres.map((g) => g.name),

                                day: parseInt(releaseDay),
                                month: parseInt(releaseMonth),
                                year: parseInt(releaseYear),

                                directors: movieData.credits.crew.filter((p) => p.job == "Director").map((p) => {
                                    return {
                                        id: `TMDB_${p.id}`,
                                        name: p.name,
                                        imageUrl: p.profile_path ? TMDB_IMAGE_BASE + p.profile_path : null
                                    };
                                }),
                                actors: movieData.credits.cast.map((p) => {
                                    return {
                                        id: `TMDB_${p.id}`,
                                        name: p.name,
                                        playedCharacter: p.character,
                                        imageUrl: p.profile_path ? TMDB_IMAGE_BASE + p.profile_path : null
                                    };
                                }),
                                studios: movieData.production_companies.map((c) => {
                                    return {
                                        id: `TMDB_${c.id}`,
                                        name: c.name,
                                        logoUrl: c.logo_path ? TMDB_IMAGE_BASE + c.logo_path : null
                                    };
                                }),

                                ratings: {
                                    IMDB: null,
                                    ROTTEN_TOMATOES: null,
                                    METACRITIC: null,
                                }
                            },
                            files: {
                                images: {
                                    posterUrl: movieData.poster_path ? TMDB_IMAGE_BASE + movieData.poster_path : null,
                                    backdropUrl: movieData.backdrop_path ? TMDB_IMAGE_BASE + movieData.backdrop_path : null,
                                },
                                streams: null,
                                subtitles: null
                            }
                        };
                    })
            );
        }

        return new JsonResponse({
            data: {
                found: await Promise.all(results)
            },
            error: null,
            rel: {}
        });
    });

}