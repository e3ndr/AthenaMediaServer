import { JsonResponse } from "./index.mjs";

async function isValidAthenaServer(url) {
    const response = await (await fetch(`${url.protocol}${url.hostname}/.well-known/x-athena`)).text();
    return response.trim() == "yes";
}

function isAcceptableToProxy(response) {
    const contentType = (response.headers.get("Content-Type") || "").split(";")[0];
    if (!["text/plain", "application/json"].includes(contentType)) {
        throw "You may only use this proxy to make non-media requests.";
    }
}

export default (/** @type {import("itty-router").RouterType<import("itty-router").Route, any[]} */router) => {

    router.get("/server-proxy", async (request) => {
        const url = new URL(new URL(request.url).searchParams.get("url"));

        console.debug("Proxying request:", url);

        try {
            switch (url.protocol) {
                case "http":
                case "https": {
                    if (!isValidAthenaServer(url)) {
                        console.debug("Not a valid Athena server.");
                        return new JsonResponse({
                            data: null,
                            error: `The server url "${url.protocol}${url.hostname}" does not appear to be an Athena server.`,
                            rel: null,
                        }, { status: 400 });
                    }

                    const response = await fetch(url, {
                        headers: new Headers({
                            ...Object.fromEntries(request.headers.entries()),
                            "X-Athena-Proxy": "used"
                        })
                    });

                    try {
                        isAcceptableToProxy(response);
                    } catch (message) {
                        console.debug("Unacceptable to proxy:", message);
                        return new JsonResponse({
                            data: null,
                            error: message,
                            rel: null,
                        }, { status: 400 });
                    }

                    console.debug("Success!");
                    return response;
                }

                default:
                    console.debug("Invalid protocol.");
                    return new JsonResponse({
                        data: null,
                        error: `Unknown or unsupported protocol "${url.protocol}"`,
                        rel: null,
                    }, { status: 400 });
            }
        } catch (e) {
            console.error("Internal error:", e.toString(), e.stack);
            return new JsonResponse({
                data: null,
                error: "An internal error occurred whilst satisfying your request.",
                rel: null,
            }, { status: 500 });
        }
    });

}