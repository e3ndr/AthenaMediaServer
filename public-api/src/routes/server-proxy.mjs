import { JsonResponse } from "./index.mjs";

export default (/** @type {import("itty-router").RouterType<import("itty-router").Route, any[]} */router) => {

    router.get("/server-proxy", async (request) => {
        const searchParams = new URL(request.url).searchParams;

        const url = new URL(searchParams.get("url"));

        try {
            switch (url.protocol) {
                case "http":
                case "https": {
                    if ((await (await fetch(`${url.protocol}${url.hostname}/.well-known/x-athena`)).text()).trim() != "yes") {
                        return new JsonResponse({
                            data: null,
                            error: `The server url "${url.protocol}${url.hostname}" does not appear to be an Athena server.`,
                            rel: null,
                        }, { status: 400 });
                    }

                    const response = await fetch(url, {
                        headers: new Headers({
                            ...Object.fromEntries(request.headers.entries()),
                            "X-Athena-Proxy-Limits": "NO_STREAMING"
                        })
                    });

                    const contentType = (response.headers.get("Content-Type") || "").split(";")[0];
                    if (!["text/plain", "application/json"].includes(contentType)) {
                        return new JsonResponse({
                            data: null,
                            error: `You may only use this proxy to make non-media requests.`,
                            rel: null,
                        }, { status: 400 });
                    }

                    return response;
                }

                default:
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
                error: `An internal error occurred whilst satisfying your request.`,
                rel: null,
            }, { status: 500 });
        }
    });

}