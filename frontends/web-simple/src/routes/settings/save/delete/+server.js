import { SAVE_HTML, setSettings, getSettings, getAvailableSettings } from "$lib/app.mjs";

export async function POST({ cookies, request }) {
    const postData = Object.fromEntries((await request.formData()).entries());
    const currentSettings = getSettings(cookies, request.headers);

    let newSettings = { ...currentSettings };
    newSettings.servers.splice(postData["arr-idx"], 1);

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