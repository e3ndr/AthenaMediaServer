import { error, redirect } from '@sveltejs/kit';

import * as API from '$lib/api.mjs';

/** @type {import('./$types').PageLoad} */
export async function load({ params }) {
	try {
		const media = await API.getMediaById({ mediaId: params.mediaId });
		if (!media) throw redirect(302, '/media');

		return {
			videoUrl: `${API.PUBLIC_API_LOCATION}/media/${media.id}/stream/raw?format=MKV&quality=SD`,
			media: media
		};
	} catch (e) {
		console.error(e);
		throw error(500, e.toString());
	}
}
