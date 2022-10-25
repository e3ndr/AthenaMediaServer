package xyz.e3ndr.athena;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;

import co.casterlabs.rakurai.json.Rson;
import xyz.e3ndr.athena.transcoding.Transcoder;
import xyz.e3ndr.athena.types.AudioCodec;
import xyz.e3ndr.athena.types.ContainerFormat;
import xyz.e3ndr.athena.types.VideoCodec;
import xyz.e3ndr.athena.types.VideoQuality;
import xyz.e3ndr.athena.types.media.Media;

public class Athena {
    public static final String STREAM_FORMAT = "mkv";
    public static final String STREAM_FORMAT_FF = "matroska";

    public static final int STREAMING_BUFFER_SIZE = 64/*kb*/ * 1000;

    public static File mediaDirectory;
    public static File cacheDirectory;

    public static boolean enableCudaAcceleration;

    public static Media getMedia(String mediaId) throws IOException {
        File mediaIndexFile = new File(mediaDirectory, mediaId.concat("/index.json"));
        String mediaIndex = Files.readString(mediaIndexFile.toPath(), StandardCharsets.UTF_8);

        return Rson.DEFAULT.fromJson(mediaIndex, Media.class);
    }

    public static FileChannel startStream(Media media, VideoQuality desiredQuality, VideoCodec desiredVCodec, AudioCodec desiredACodec, ContainerFormat desiredContainer, int... streamIds) throws IOException {
        final File cacheFile = Transcoder.getFile(media, desiredQuality, desiredVCodec, desiredACodec, desiredContainer, streamIds);

        if (!cacheFile.exists()) {
            Transcoder.start(cacheFile, media, desiredQuality, desiredVCodec, desiredACodec, desiredContainer, streamIds);
        }

        return FileChannel.open(cacheFile.toPath(), EnumSet.of(StandardOpenOption.READ));
    }

    public static boolean authenticate(String token) {
        return true; // TODO
    }

    public static List<Media> listMedia() {
        List<Media> result = new LinkedList<>();

        for (String mediaId : mediaDirectory.list()) {
            try {
                result.add(getMedia(mediaId));;
            } catch (IOException ignored) {}
        }

        return result;
    }

}
