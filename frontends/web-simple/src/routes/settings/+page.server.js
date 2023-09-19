import { getSettings, getAvailableSettings } from "$lib/app.mjs";

/** @type {import('./$types').PageServerLoad} */
export async function load({ cookies, request }) {
    return {
        settings: getSettings(cookies, request.headers),
        availableSettings: getAvailableSettings(request.headers)
    };
};
