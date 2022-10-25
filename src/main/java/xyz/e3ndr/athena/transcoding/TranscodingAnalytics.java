package xyz.e3ndr.athena.transcoding;

import java.util.concurrent.TimeUnit;

import co.casterlabs.rakurai.json.annotating.JsonClass;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@JsonClass(exposeAll = true)
class TranscodingAnalytics {
    private static final long START_AFTER = TimeUnit.SECONDS.toMillis(5); // Wait for 5s of footage to be ripped.

    private int bitrate = 0;
    private long sentBytes = 0;
    private long currentTime = 0;
    private double encodingSpeed = 0;

    public boolean hasStarted() {
        return this.currentTime > START_AFTER;
    }

    public void update(String ffmpegLine) {
        /* frame=14371 fps= 26 q=-1.0 size=  177701kB time=00:09:59.26 bitrate=2429.2kbits/s speed=1.07x */

        {
            String bitrateStr = ffmpegLine.split("bitrate= *")[1].split(" ")[0]; // "2429.2kbits/s"
            if (bitrateStr.equals("N/A")) return; // Empty stat.

            bitrateStr = bitrateStr.substring(0, bitrateStr.length() - "kbits/s".length());

            this.bitrate = (int) (Double.parseDouble(bitrateStr) * 1000);
        }

        {
            String sizeStr = ffmpegLine.split("size= *")[1].split(" ")[0]; // "177701kB"
            sizeStr = sizeStr.substring(0, sizeStr.length() - "kB".length());

            this.sentBytes = Long.parseLong(sizeStr) * 1000;
        }

        {
            String timeStr = ffmpegLine.split("time= *")[1].split(" ")[0]; // "00:09:59.26"
            String[] timeParts = timeStr.split("[:\\.]");

            int hours = Integer.parseInt(timeParts[0]);             // 00
            int minutes = Integer.parseInt(timeParts[1]);           // 09
            int seconds = Integer.parseInt(timeParts[2]);           // 59
            int milliseconds = Integer.parseInt(timeParts[3]) * 10; // 26 -> 260

            long totalTime = 0;
            totalTime += milliseconds;
            totalTime += TimeUnit.SECONDS.toMillis(seconds);
            totalTime += TimeUnit.MINUTES.toMillis(minutes);
            totalTime += TimeUnit.HOURS.toMillis(hours);

            this.currentTime = totalTime;
        }

        {
            String speedStr = ffmpegLine.split("speed= *")[1].split(" ")[0]; // "1.07x"
            speedStr = speedStr.substring(0, speedStr.length() - "x".length());

            this.encodingSpeed = Double.parseDouble(speedStr);
        }
    }

}
