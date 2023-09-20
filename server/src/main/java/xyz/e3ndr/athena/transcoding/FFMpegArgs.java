package xyz.e3ndr.athena.transcoding;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import xyz.e3ndr.athena.types.AudioCodec;
import xyz.e3ndr.athena.types.VideoCodec;
import xyz.e3ndr.athena.types.VideoQuality;

public class FFMpegArgs {

    public static List<String> a_getFF(AudioCodec codec) {
        switch (codec) {
            case SOURCE:
                return Arrays.asList("-c:a", "copy");

            case OPUS:
                return Arrays.asList("-c:a", "libopus", "-b:a", "128k");

            case AAC:
                return Arrays.asList("-c:a", "aac", "-b:a", "96k");

            case MP3:
                return Arrays.asList("-c:a", "mp3", "-b:a", "96k");
        }
        return null;
    }

    public static List<String> v_getFF(VideoCodec codec, VideoQuality quality, boolean enableCuda) {
        if (quality == VideoQuality.SOURCE) {
            return Arrays.asList("-c:v", "copy");
        }

        // TODO the more advanced parameters for HEVC and AV1

        switch (codec) {
            case H264_BASELINE:
            case H264_MAIN:
                return getH264Args(codec, quality, enableCuda);

            case HEVC:
                if (enableCuda) {
                    return Arrays.asList("-c:v", "hevc_nvenc");
                } else {
                    return Arrays.asList("-c:v", "hevc");
                }

            case AV1:
                return Arrays.asList("-c:v", "libsvtav1");

            case SPARK:
                return Arrays.asList("-c:v", "flv1");
        }
        return null;
    }

    private static List<String> getH264Args(VideoCodec codec, VideoQuality quality, boolean enableCuda) {
        List<String> args = new LinkedList<>();

        if (enableCuda) {
            args.add("-c:v");
            args.add("h264_nvenc");
        } else {
            args.add("-c:v");
            args.add("h264");
        }

        switch (codec) {
            case H264_BASELINE:
                args.add("-profile:v");
                args.add("baseline");
                args.add("-level:v");
                args.add("1.0");
                break;

            case H264_MAIN:
                args.add("-profile:v");
                args.add("main");
                // Let it pick the level.
                break;

            default:
                break;
        }

        return args;
    }

}
