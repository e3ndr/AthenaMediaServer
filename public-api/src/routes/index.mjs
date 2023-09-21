import { Router } from 'itty-router';

export class JsonResponse extends Response {
  constructor(body, init) {
    const jsonBody = JSON.stringify(body);
    init = init || {
      headers: {
        'content-type': 'application/json;charset=utf-8',
      },
    };
    super(jsonBody, init);
  }
}

const router = Router();

(await import("./search.mjs")).default(router);
(await import("./server-proxy.mjs")).default(router);

router.get('/', () => {
  return new Response(`🍿 @ ${Date.now()}`);
});

router.all('*', () => {
  console.log("Unknown route, 404'ing.");
  return new Response(null, { status: 404 });
});

export default router;