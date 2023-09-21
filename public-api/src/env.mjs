export let TMDB_API_KEY;

export function configure(env) {
    console.debug("Reading env.");

    TMDB_API_KEY = env.TMDB_API_KEY;

    console.debug("Registered env.");
}