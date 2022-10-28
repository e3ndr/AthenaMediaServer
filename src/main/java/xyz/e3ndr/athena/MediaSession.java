package xyz.e3ndr.athena;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.EnumSet;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.annotating.JsonExclude;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.ToString;
import xyz.e3ndr.athena.transcoding.TranscodeSession;
import xyz.e3ndr.athena.types.AudioCodec;
import xyz.e3ndr.athena.types.ContainerFormat;
import xyz.e3ndr.athena.types.VideoCodec;
import xyz.e3ndr.athena.types.VideoQuality;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

@Getter
@ToString
@JsonClass(exposeAll = true)
public class MediaSession {
    private static final int MAX_FAILS = 100; // ~10s

    private final String id = UUID.randomUUID().toString().replace("-", "");

    private @Nullable TranscodeSession transcodeSession; // Null means there was a cached version available.
    private boolean isCached;

    private String mediaId;
    private VideoQuality videoQuality;
    private VideoCodec videoCodec;
    private AudioCodec audioCodec;
    private int[] streamIds;

    private long bytesWritten = 0;

    @JsonExclude
    private File file;

    @JsonExclude
    @Getter(AccessLevel.NONE)
    public final FastLogger logger = new FastLogger("Media Session: ".concat(this.id));

    public MediaSession(File file, @Nullable TranscodeSession transcodeSession, String mediaId, VideoQuality desiredQuality, VideoCodec desiredVCodec, AudioCodec desiredACodec, ContainerFormat desiredContainer, int... streamIds) throws IOException {
        this.file = file;
        this.transcodeSession = transcodeSession;
        this.isCached = this.transcodeSession == null;
        this.mediaId = mediaId;
        this.videoQuality = desiredQuality;
        this.videoCodec = desiredVCodec;
        this.audioCodec = desiredACodec;
        this.streamIds = streamIds;
    }

    public void start(long skip, long maxLength, OutputStream target) {
        Athena.mediaSessions.add(this);

        try (
            FileChannel fileChannel = FileChannel.open(this.file.toPath(), EnumSet.of(StandardOpenOption.READ));
            target //
        ) {
            fileChannel.position(skip);
            this.logger.debug("Started stream at %d.", skip);

            ByteBuffer buffer = ByteBuffer.allocate(Athena.STREAMING_BUFFER_SIZE);
            byte[] bufferArray = buffer.array();
            int failCount = 0;

            while (this.bytesWritten < maxLength) {
                int read = fileChannel.read(buffer);

                if (read <= 0) {
                    failCount++;

                    if (failCount == MAX_FAILS) {
                        this.logger.debug("Ending session, out of data.");
                        break;
                    } else {
                        // Try to wait for more data to get buffered.
                        this.logger.trace("Out of data! Sleeping.");
                        Thread.sleep(100);
                        continue;
                    }
                } else {
                    failCount = 0;
                }

                target.write(bufferArray, 0, read);
                target.flush();
                buffer.clear();
                this.bytesWritten += read;

                this.logger.trace("Wrote data! %d bytes sent so far.", bytesWritten);
            }

            this.logger.debug("Ended stream, target reached.");
        } catch (IOException | InterruptedException e) {
            this.logger.debug("Ended stream, exception: %s: %s", e.getClass().getSimpleName(), e.getMessage());
        } finally {
            Athena.mediaSessions.remove(this);
        }
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

}
