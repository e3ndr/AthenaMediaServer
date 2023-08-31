import { error, redirect } from '@sveltejs/kit';

import * as API from '$lib/api.mjs';

/** @type {import('./$types').PageLoad} */
export async function load({ params, fetch }) {
	try {
		const media = API.getMediaById({ mediaId: params.mediaId, fetch });
		if (!media) throw redirect(302, '/media');

		return {
			media
		};
	} catch (e) {
		throw error(500, e);
	}
}
