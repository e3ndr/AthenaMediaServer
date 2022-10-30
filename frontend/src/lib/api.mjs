import { PUBLIC_API_LOCATION as __PUBLIC_API_LOCATION } from '$env/static/public';
export const PUBLIC_API_LOCATION = __PUBLIC_API_LOCATION || '/api';

async function json(fetch, /** @type {string} */ url, /** @type {RequestInit | undefined} */ init) {
    return await (await fetch(PUBLIC_API_LOCATION + url, init)).json();
}

export function listMedia({ fetch = window.fetch }) {
    return json(fetch, '/media');
}

export function getMediaById({ mediaId, fetch = window.fetch }) {
    return json(fetch, `/media/${mediaId}`);
}