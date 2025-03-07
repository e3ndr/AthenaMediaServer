package xyz.e3ndr.athena.service.http;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonArray;
import co.casterlabs.rakurai.json.element.JsonObject;
import co.casterlabs.rhs.protocol.HttpMethod;
import co.casterlabs.rhs.protocol.StandardHttpStatus;
import co.casterlabs.rhs.server.HttpResponse;
import co.casterlabs.rhs.server.HttpResponse.ResponseContent;
import co.casterlabs.sora.api.http.HttpProvider;
import co.casterlabs.sora.api.http.SoraHttpSession;
import co.casterlabs.sora.api.http.annotations.HttpEndpoint;
import xyz.e3ndr.athena.Athena;
import xyz.e3ndr.athena.types.media.Media;

class IngestApiRoutes implements HttpProvider {

    @HttpEndpoint(uri = "/api/ingest/file/:fileName", allowedMethods = {
            HttpMethod.POST
    })
    public HttpResponse onIngest(SoraHttpSession session) {
        String fileName = URLDecoder.decode(session.getUriParameters().get("fileName"), StandardCharsets.UTF_8);

        try {
            JsonObject body = (JsonObject) session.getRequestBodyJson(Rson.DEFAULT);
            Media media = Rson.DEFAULT.fromJson(body, Media.class);

            Athena.ingest(fileName, media);

            return HttpResponse
                .newFixedLengthResponse(StandardHttpStatus.OK)
                .putHeader("Access-Control-Allow-Origin", session.getHeaders().getOrDefault("Origin", Arrays.asList("*")).get(0));
        } catch (Exception e) {
            return HttpResponse.newFixedLengthResponse(StandardHttpStatus.INTERNAL_ERROR);
        }
    }

    @HttpEndpoint(uri = "/api/ingest/file/:fileName")
    public HttpResponse onGetIngestableInformation(SoraHttpSession session) {
        String fileName = URLDecoder.decode(session.getUriParameters().get("fileName"), StandardCharsets.UTF_8);

        JsonArray ingestable = Athena.getIngestableInfo(fileName);

        if (ingestable == null) {
            return HttpResponse.newFixedLengthResponse(StandardHttpStatus.NOT_FOUND);
        } else {
            return HttpResponse
                .newFixedLengthResponse(
                    StandardHttpStatus.OK,
                    ingestable
                )
                .setMimeType("application/json")
                .putHeader("Access-Control-Allow-Origin", session.getHeader("Origin"));
        }
    }

    @HttpEndpoint(uri = "/api/ingest/file/:fileName/stream/:streamId")
    public HttpResponse onStreamFromIngestable(SoraHttpSession session) {
        String fileName = URLDecoder.decode(session.getUriParameters().get("fileName"), StandardCharsets.UTF_8);
        int streamId = Integer.parseInt(session.getUriParameters().get("streamId"));

        return new HttpResponse(
            new ResponseContent() {
                @Override
                public void write(OutputStream out) throws IOException {
                    Athena.streamIngestable(fileName, streamId, out);
                }

                @Override
                public long getLength() {
                    return -1;
                }

                @Override
                public void close() throws IOException {}  // Already handled in Athena.streamIngestable
            },
            StandardHttpStatus.OK
        )
            .setMimeType("video/webm")
            .putHeader("Access-Control-Allow-Origin", session.getHeaders().getOrDefault("Origin", Arrays.asList("*")).get(0));
    }

    @HttpEndpoint(uri = "/api/ingest/list")
    public HttpResponse onListIngestables(SoraHttpSession session) {
        return HttpResponse
            .newFixedLengthResponse(
                StandardHttpStatus.OK,
                Rson.DEFAULT.toJson(Athena.listIngestables())
            )
            .setMimeType("application/json")
            .putHeader("Access-Control-Allow-Origin", session.getHeaders().getOrDefault("Origin", Arrays.asList("*")).get(0));
    }

}
