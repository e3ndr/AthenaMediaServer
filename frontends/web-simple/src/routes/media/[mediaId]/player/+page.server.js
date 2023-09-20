import { error, redirect } from '@sveltejs/kit';

import { getSettings } from "$lib/app.mjs";
import * as API from '$lib/api.mjs';

/** @type {import('./$types').PageLoad} */
export async function load({ cookies, request, params }) {
	try {
		const settings = getSettings(cookies, request.headers);

		const media = await API.getMediaById(settings.servers[0], { mediaId: params.mediaId });
		if (!media) throw redirect(302, '/media');

		const videoUrlParams = [
			`format=${settings.deliveryPreferences.c}`,
			`videoCodec=${settings.deliveryPreferences.v}`,
			`audioCodec=${settings.deliveryPreferences.a}`,
			`quality=${settings.preferredQuality}`
		];

		const server = "http://192.168.0.214:8125/api" // settings.servers[0];

		let playerType;
		switch (settings.deliveryPreferences.c) {
			case "FLV":
				playerType = "FLASH";
				break;

			case "SWF":
				playerType = "SWF";
				break;

			default:
				playerType = "HTML5";
				break;
		}

		return {
			playerType: playerType,
			videoUrl: `${server}/media/${media.id}/stream/raw?${videoUrlParams.join("&")}`,
			media: media
		};
	} catch (e) {
		console.error(e);
		throw error(500, e.toString());
	}
}
