import { setSettings, getSettings, getAvailableSettings } from "$lib/app.mjs";

const HTML = `<!DOCTYPE html>
<html>
    <script>
        alert("Successfully saved your settings!");
        location.href = "/settings";
    </script>
    <a href="/settings>Click here if not automatically redirected</a>
</html>
`;

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
        HTML,
        {
            headers: {
                "Content-Type": "text/html"
            }
        }
    );
}