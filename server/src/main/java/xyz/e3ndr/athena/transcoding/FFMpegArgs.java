package xyz.e3ndr.athena.transcoding;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import xyz.e3ndr.athena.Config.TranscodeConfig.TranscodeAcceleration;
import xyz.e3ndr.athena.types.AudioCodec;
import xyz.e3ndr.athena.types.VideoCodec;
import xyz.e3ndr.athena.types.VideoQuality;

public class FFMpegArgs {

    public static List<String> a_getFF(AudioCodec codec) {
        switch (codec) {
            case SOURCE:
                return Arrays.asList("-c:a", "copy");

            case OPUS:
                return Arrays.asList("-c:a", "libopus", "-b:a", "320k");

            case AAC:
                return Arrays.asList("-c:a", "aac", "-ac", "2", "-b:a", "156k");

            case MP3:
                return Arrays.asList("-c:a", "mp3", "-ac", "2", "-b:a", "128k");
        }
        return null;
    }

    public static List<String> v_getFF(VideoCodec codec, VideoQuality quality, TranscodeAcceleration acceleration) {
        // TODO the more advanced parameters for HEVC and AV1

        switch (codec) {
            case SOURCE:
                return Arrays.asList("-c:v", "copy");

            case H264_BASELINE:
            case H264_HIGH:
                return getH264Args(codec, quality, acceleration);

            case HEVC:
                switch (acceleration) {
                    case NVIDIA_PREFERRED:
                        return Arrays.asList("-c:v", "hevc_nvenc");

                    case SOFTWARE_ONLY:
                        return Arrays.asList("-c:v", "hevc");
                }

            case AV1:
                return Arrays.asList("-c:v", "libsvtav1");

            case SPARK:
                return Arrays.asList("-c:v", "flv1");
        }
        return null;
    }

    private static List<String> getH264Args(VideoCodec codec, VideoQuality quality, TranscodeAcceleration acceleration) {
        List<String> args = new LinkedList<>();

        switch (acceleration) {
            case NVIDIA_PREFERRED:
                args.add("-c:v");
                args.add("h264_nvenc");
                break;

            case SOFTWARE_ONLY:
                args.add("-c:v");
                args.add("h264");
                break;
        }

        switch (codec) {
            case H264_BASELINE:
                args.add("-profile:v");
                args.add("baseline");
//                args.add("-level:v");
//                args.add("1.0");
                break;

            case H264_HIGH:
                args.add("-profile:v");
                args.add("high");
                args.add("-level");
                args.add("5.0");
                if (acceleration != TranscodeAcceleration.NVIDIA_PREFERRED) {
                    // NVIDIA does not support tune.
                    args.add("-tune");
                    args.add("film");
                }
                args.add("-preset");
                args.add("slow");
                break;

            default:
                break;
        }

        return args;
    }

}
