package xyz.e3ndr.athena.service.http;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import co.casterlabs.rakurai.io.http.HttpStatus;
import co.casterlabs.rakurai.io.http.server.HttpResponse;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.NonNull;

public class JsonResponse extends HttpResponse {

    public JsonResponse(@NonNull HttpStatus status, @NonNull JsonElement data, @NonNull Map<String, String> rel) {
        super(
            new ByteResponse(
                new JsonObject()
                    .put("data", data)
                    .putNull("error")
                    .put("rel", Rson.DEFAULT.toJson(rel))
                    .toString(true)
                    .getBytes(StandardCharsets.UTF_8)
            ),
            status
        );
        this.setMimeType("application/json;charset=utf-8");
    }

    /**
     * @param status
     * @param error  A human-friendly string.
     */
    public JsonResponse(@NonNull HttpStatus status, @NonNull String error) {
        super(
            new ByteResponse(
                new JsonObject()
                    .putNull("data")
                    .put("error", error)
                    .putNull("rel")
                    .toString(true)
                    .getBytes(StandardCharsets.UTF_8)
            ),
            status
        );
        this.setMimeType("application/json;charset=utf-8");
    }

}
