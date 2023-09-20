import { error, redirect } from '@sveltejs/kit';

import { getSettings } from "$lib/app.mjs";
import * as API from '$lib/api.mjs';

/** @type {import('./$types').PageLoad} */
export async function load({ cookies, request, params }) {
	try {
		const settings = getSettings(cookies, request.headers);

		const media = await API.getMediaById(settings.servers[0], { mediaId: params.mediaId });
		if (!media) throw redirect(302, '/media');

		return {
			media
		};
	} catch (e) {
		console.error(e);
		throw error(500, e.toString());
	}
}
