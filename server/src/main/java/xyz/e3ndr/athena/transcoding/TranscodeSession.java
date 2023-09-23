package xyz.e3ndr.athena.transcoding;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.annotating.JsonExclude;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.ToString;
import xyz.e3ndr.athena.types.AudioCodec;
import xyz.e3ndr.athena.types.ContainerFormat;
import xyz.e3ndr.athena.types.VideoCodec;
import xyz.e3ndr.athena.types.VideoQuality;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

@Getter
@ToString
@JsonClass(exposeAll = true)
public class TranscodeSession {
    private final String id = UUID.randomUUID().toString().replace("-", "");

    private String mediaId;
    private String file;
    private VideoQuality videoQuality;
    private VideoCodec videoCodec;
    private AudioCodec audioCodec;
    private int[] streamIds;
    private long duration;

    private int averageBitrate = 0;
    private long bytesWritten = 0;
    private double encodingProgress = 0;
    private double encodingSpeed = 0;

    private boolean isComplete;

    @JsonExclude
    @ToString.Exclude
    @Getter(AccessLevel.NONE)
    public final FastLogger logger = new FastLogger("Transcode Session: ".concat(this.id));

    public TranscodeSession(String mediaId, File targetFile, VideoQuality desiredQuality, VideoCodec desiredVCodec, AudioCodec desiredACodec, ContainerFormat desiredContainer, int... streamIds) {
        this.mediaId = mediaId;
        this.file = targetFile.toString();
        this.videoQuality = desiredQuality;
        this.videoCodec = desiredVCodec;
        this.audioCodec = desiredACodec;
        this.streamIds = streamIds;
    }

    public void processStatistic(String ffmpegLine) {
        /* frame=14371 fps= 26 q=-1.0 size=  177701kB time=00:09:59.26 bitrate=2429.2kbits/s speed=1.07x */

        {
            String bitrateStr = ffmpegLine.split("bitrate= *")[1].split(" ")[0]; // "2429.2kbits/s"
            if (bitrateStr.equals("N/A")) return; // Empty stat.

            bitrateStr = bitrateStr.substring(0, bitrateStr.length() - "kbits/s".length());

            this.averageBitrate = (int) (Double.parseDouble(bitrateStr) * 1000);
        }

        {
            String sizeStr = ffmpegLine.split("size= *")[1].split(" ")[0]; // "177701kB"
            sizeStr = sizeStr.substring(0, sizeStr.length() - "kB".length());

            this.bytesWritten = Long.parseLong(sizeStr) * 1000;
        }

        {
            String timeStr = ffmpegLine.split("time= *")[1].split(" ")[0]; // "00:09:59.26"

            long currentTime = timestampToMillis(timeStr);
            this.encodingProgress = (double) currentTime / this.duration;

            if (this.encodingProgress == 1) {
                this.isComplete = true;
            }
        }

        {
            String speedStr = ffmpegLine.split("speed= *")[1].split(" ")[0]; // "1.07x"
            speedStr = speedStr.substring(0, speedStr.length() - "x".length());

            this.encodingSpeed = Double.parseDouble(speedStr);
        }
    }

    public void init(List<String> initInfo) {
        /*
        Input #0, matroska,webm, from '..\IMDB_tt0427327\streams\0.mkv':
          Metadata:
            COMPATIBLE_BRANDS: isomavc1
            MAJOR_BRAND     : isom
            MINOR_VERSION   : 1
            ENCODER         : Lavf58.76.100
          Duration: 01:56:07.00, start: 0.083000, bitrate: 2170 kb/s
          Stream #0:0: Video: h264 (High), yuv420p(tv, bt709/bt709/unknown, progressive), 1920x816 [SAR 1:1 DAR 40:17], 23.98 fps, 23.98 tbr, 1k tbn, 47.95 tbc (default)
            Metadata:
              HANDLER_NAME    : video.264#trackID=1:fps=23.976 - Imported with GPAC 0.5.0-rev4065
              VENDOR_ID       : [0][0][0][0]
              DURATION        : 01:56:07.002000000
        Input #1, matroska,webm, from '..\IMDB_tt0427327\streams\1.mkv':
          Metadata:
            COMPATIBLE_BRANDS: isomavc1
            MAJOR_BRAND     : isom
            MINOR_VERSION   : 1
            ENCODER         : Lavf58.76.100
          Duration: 01:56:07.02, start: 0.000000, bitrate: 96 kb/s
          Stream #1:0(eng): Audio: aac (LC), 48000 Hz, stereo, fltp (default)
            Metadata:
              HANDLER_NAME    : GPAC ISO Audio Handler
              VENDOR_ID       : [0][0][0][0]
              DURATION        : 01:56:07.018000000
        Stream mapping:
          Stream #0:0 -> #0:0 (h264 (native) -> h264 (h264_nvenc))
          Stream #1:0 -> #0:1 (copy)
        Press [q] to stop, [?] for help
        Output #0, matroska, to 'pipe:1':
          Metadata:
            COMPATIBLE_BRANDS: isomavc1
            MAJOR_BRAND     : isom
            MINOR_VERSION   : 1
            encoder         : Lavf58.76.100
          Stream #0:0: Video: h264 (Main) (H264 / 0x34363248), yuv420p(tv, bt709/bt709/unknown, progressive), 240x102 [SAR 1:1 DAR 40:17], q=2-31, 450 kb/s, 23.98 fps, 1k tbn (default)
            Metadata:
              HANDLER_NAME    : video.264#trackID=1:fps=23.976 - Imported with GPAC 0.5.0-rev4065
              VENDOR_ID       : [0][0][0][0]
              DURATION        : 01:56:07.002000000
              encoder         : Lavc58.134.100 h264_nvenc
            Side data:
              cpb: bitrate max/min/avg: 0/0/450000 buffer size: 512000 vbv_delay: N/A
          Stream #0:1(eng): Audio: aac (LC) ([255][0][0][0] / 0x00FF), 48000 Hz, stereo, fltp (default)
            Metadata:
              HANDLER_NAME    : GPAC ISO Audio Handler
              VENDOR_ID       : [0][0][0][0]
              DURATION        : 01:56:07.018000000
         */

        initInfo.add(""); // Add a blank line, fixes some logic.

        List<String> currentSection = new LinkedList<>();
        for (String line : initInfo) {
            // Lines that start with spaces are additional info for the current section.
            if (line.startsWith(" ")) {
                currentSection.add(line);
            } else {
                if (!currentSection.isEmpty()) {
                    // We've begun a new section...

                    FastLogger.logStatic(LogLevel.DEBUG, "Init info section:\n%s", String.join("\n", currentSection));

                    // Process it.
                    String sectionStart = currentSection.remove(0);

                    if (sectionStart.startsWith("Output")) {
                        for (String sectionEntry : currentSection) {
                            sectionEntry = sectionEntry.trim();

                            if (sectionEntry.startsWith("DURATION")) {
                                // "DURATION : 01:56:07.018000000"
                                String durationStr = sectionEntry.substring(sectionEntry.indexOf(":") + 2); // "01:56:07.018000000"
                                long duration = timestampToMillis(durationStr);

                                // Store the longest duration.
                                if (duration > this.duration) {
                                    this.duration = duration;
                                }
                            }
                        }
                    }

                    // Clear and start anew.
                    currentSection.clear();
                }

                currentSection.add(line);
            }
        }
    }

    private static long timestampToMillis(String timeStr) {
        String[] timeParts = timeStr.split("[:\\.]"); // "00:09:59.26"

        int hours = Integer.parseInt(timeParts[0]);             // 00
        int minutes = Integer.parseInt(timeParts[1]);           // 09
        int seconds = Integer.parseInt(timeParts[2]);           // 59
//        int milliseconds = Integer.parseInt(timeParts[3]) * 10; // 26 -> 260

        long totalTime = 0;
//        totalTime += milliseconds;
        totalTime += TimeUnit.SECONDS.toMillis(seconds);
        totalTime += TimeUnit.MINUTES.toMillis(minutes);
        totalTime += TimeUnit.HOURS.toMillis(hours);

        return totalTime;
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

}
