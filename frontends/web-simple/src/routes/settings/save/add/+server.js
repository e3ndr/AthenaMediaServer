import { SAVE_HTML, setSettings, getSettings, getAvailableSettings } from "$lib/app.mjs";

export async function POST({ cookies, request }) {
    const postData = Object.fromEntries((await request.formData()).entries());
    const currentSettings = getSettings(cookies, request.headers);

    let newSettings = { ...currentSettings };

    // TODO validate by connecting to the server and handshaking.
    newSettings.servers.push(postData.server);

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