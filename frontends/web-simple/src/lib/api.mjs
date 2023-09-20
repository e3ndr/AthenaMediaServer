
async function json(/** @type {string} */ url, /** @type {RequestInit | undefined} */ init) {
    return await (await fetch(url, init)).json();
}

export function listMedia(server) {
    return json(server + '/media');
}

export function getMediaById(server, { mediaId }) {
    return json(server + `/media/${mediaId}`);
}