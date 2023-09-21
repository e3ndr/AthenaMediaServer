
async function json(/** @type {string} */ url, /** @type {RequestInit | undefined} */ init) {
    const response = await (await fetch(url, init)).json();

    if (response.data) {
        return response.data;
    } else {
        throw response.error;
    }
}

export async function listMedia(server) {
    return (await json(server + '/media')).list;
}

export function getMediaById(server, { mediaId }) {
    return json(server + `/media/${mediaId}`);
}