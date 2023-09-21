import { redirect } from "@sveltejs/kit";

import { getSettings } from "$lib/app.mjs";
import * as API from '$lib/api.mjs';

/** @type {import('./$types').PageServerLoad} */
export async function load({ cookies, request }) {
    const settings = getSettings(cookies, request.headers);

    if (settings.servers.length == 0) {
        throw new redirect(301, "/setup");
    }

    const mediaList = [];
    const errors = [];

    for (const server of settings.servers) {
        try {
            mediaList.push(...await API.listMedia(server));
        } catch (e) {
            console.error(e);
            errors.push(e.toString());
        }
    }

    return {
        mediaList,
        errors
    };
};
