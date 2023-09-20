import { DEFAULT_SERVERS_CSV } from '$env/static/private';

export const SAVE_HTML = `<!DOCTYPE html>
<html>
    <script>
        alert("Successfully saved your settings!");
        location.href = "/settings";
    </script>
    <a href="/settings">Click here if not automatically redirected</a>
</html>
`;

export function setSettings(newSettings, cookies) {
    // cookies.delete("settings", { path: "/" });
    cookies.set(
        "settings",
        JSON.stringify(newSettings),
        {
            path: "/",
            httpOnly: false,
            secure: true,
            sameSite: false,
            expires: new Date(Date.now() + (365 * 24 * 60 * 60 * 1000))
        }
    );
}

export function getSettings(cookies, headers) {
    const settings = JSON.parse(cookies.get("settings") || "{}");

    let defaultServers = [];

    if (DEFAULT_SERVERS_CSV.length > 0) {
        defaultServers = DEFAULT_SERVERS_CSV.split(",");
    }

    return {
        // Defaults...
        servers: defaultServers,
        ...getDefaultSettings(headers),

        // Add...
        ...settings
    };
}

function getDefaultSettings(headers) {
    const availableSettings = getAvailableSettings(headers);

    return {
        preferredQuality: availableSettings.qualities[0],
        deliveryPreferences: {
            c: availableSettings.containers[0],
            v: availableSettings.videoCodecs[0],
            a: availableSettings.audioCodecs[0],
        },
    };
}

export function getAvailableSettings(headers) {
    // Try and sniff out some defaults from the headers.
    const userAgent = headers.get("User-Agent") || "";

    if (userAgent.includes("PlayStation Portable")) {
        return {
            qualities: ["LD"],
            containers: ["SWF"],
            videoCodecs: ["SPARK"],
            audioCodecs: ["MP3"],
        };
    }

    if (userAgent.includes("Nintendo WiiU")) {
        return {
            qualities: ["HD"],
            containers: ["TS"],
            videoCodecs: ["H264_BASELINE"],
            audioCodecs: ["AAC"],
        };
    }

    if (userAgent.includes("Nintendo Wii")) {
        return {
            qualities: ["SD"],
            containers: ["FLV"],
            videoCodecs: ["H264_BASELINE"],
            audioCodecs: ["AAC"],
        };
    }

    if (userAgent.includes("Windows Phone 10")) {
        return {
            qualities: ["FHD", "HD", "SD", "LD"],
            containers: ["MP4"],
            videoCodecs: ["H264_BASELINE"],
            audioCodecs: ["AAC"],
        };
    }

    return {
        qualities: ["SOURCE", "UHD", "FHD", "HD", "SD", "LD"],
        containers: ["MKV", "MP4"],
        videoCodecs: ["AV1", "HEVC", "H264_MAIN"],
        audioCodecs: ["OPUS", "AAC", "MP3"],
    };
}