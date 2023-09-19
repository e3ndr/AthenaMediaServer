export function setSettings(newSettings, cookies) {
    cookies.delete("settings", { path: "/" });
    cookies.set("settings", JSON.stringify(newSettings), { path: "/" });
}

export function getSettings(cookies, headers) {
    const settings = JSON.parse(cookies.get("settings") || "{}");

    return {
        // Defaults...
        servers: [],
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

    if (userAgent.includes("Nintendo WiiU")) {
        return {
            qualities: ["FHD", "HD", "SD", "LD"],
            containers: ["TS"],
            videoCodecs: ["H264"],
            audioCodecs: ["AAC"],
        };
    }

    return {
        qualities: ["SOURCE", "UHD", "FHD", "HD", "SD", "LD"],
        containers: ["MKV", "MP4", "TS"],
        videoCodecs: ["H264", "HEVC", "SPARK", "THEORA", "VP8"],
        audioCodecs: ["AAC", "MP3", "OPUS"],
    };
}