import { error } from '@sveltejs/kit';

import * as API from '$lib/api.mjs';

/** @type {import('./$types').PageLoad} */
export async function load({ fetch }) {
	try {
		return {
			mediaList: API.listMedia({ fetch })
		};
	} catch (e) {
		throw error(500, e);
	}
}
