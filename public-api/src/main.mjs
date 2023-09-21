import * as env from "./env.mjs";

function handleCORS(request, response) {
    response.headers.set("Access-Control-Allow-Origin", "*");
    response.headers.set("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, PATCH, DELETE, OPTIONS");
    response.headers.set("Access-Control-Max-Age", "86400");
    response.headers.set("Access-Control-Allow-Headers", request.headers.get("Access-Control-Request-Headers"));
}

export default {
    fetch: async function (request, _env) {
        let response;

        try {
            console.log("Incoming request!");

            if (request.method == "OPTIONS") {
                console.log("Handling options!");
                response = new Response(null);
            } else {
                console.log("Handling request!");
                env.configure(_env);

                const router = (await import("./routes/index.mjs")).default;
                response = await router.handle(request, env);
            }

        } catch (e) {
            console.error(e);
            return new Response("Internal Error.");
        }

        handleCORS(request, response);

        return response;
    },
};
