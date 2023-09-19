
async function json(/** @type {string} */ url, /** @type {RequestInit | undefined} */ init) {
    return await (await fetch(PUBLIC_API_LOCATION + url, init)).json();
}

export function listMedia() {
    return json('/media');
}

export function getMediaById({ mediaId }) {
    return json(`/media/${mediaId}`);
}