import { SAVE_HTML, setSettings, getSettings, getAvailableSettings } from "$lib/app.mjs";

export async function POST({ cookies, request }) {
    const postData = Object.fromEntries((await request.formData()).entries());
    const currentSettings = getSettings(cookies, request.headers);

    let newSettings = { ...currentSettings };

    // TODO validate with getAvailableSettings().

    if (postData["media-quality"]) {
        newSettings.preferredQuality = postData['media-quality'];
    }

    if (postData["container-format"]) {
        newSettings.deliveryPreferences.c = postData['container-format'];
    }

    if (postData["video-codec"]) {
        newSettings.deliveryPreferences.v = postData['video-codec'];
    }

    if (postData["audio-codec"]) {
        newSettings.deliveryPreferences.a = postData['audio-codec'];
    }

    setSettings(newSettings, cookies);

    return new Response(
        SAVE_HTML,
        {
            headers: {
                "Content-Type": "text/html"
            }
        }
    );
}