
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
