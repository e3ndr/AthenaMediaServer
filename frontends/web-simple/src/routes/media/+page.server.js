import { error } from '@sveltejs/kit';

import * as API from '$lib/api.mjs';

/** @type {import('./$types').PageLoad} */
export async function load() {
	try {
		return {
			mediaList: await API.listMedia()
		}
	} catch (e) {
		console.error(e);
		throw error(500, e.toString());
	}
}
