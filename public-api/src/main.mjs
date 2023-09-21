import * as env from "./env.mjs";

export default {
    fetch: async function (request, _env) {
        try {
            console.log("Incoming request!");
            env.configure(_env);

            console.log("Handling request...");

            const router = (await import("./routes/index.mjs")).default;
            return await router.handle(request, env);
        } catch (e) {
            console.error("Internal error:", e.toString(), e.stack);
            return new Response("Internal Error.");
        }
    },
};
