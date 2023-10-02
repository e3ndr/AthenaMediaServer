package xyz.e3ndr.athena;

import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.annotating.JsonField;
import lombok.ToString;

@ToString
@JsonClass(exposeAll = true)
public class Config {
    public ConsoleConfig console = new ConsoleConfig();
    public ServiceConfig services = new ServiceConfig();
    public SessionConfig sessions = new SessionConfig();
    public TranscodeConfig transcoding = new TranscodeConfig();

    @ToString
    @JsonClass(exposeAll = true)
    public static class ConsoleConfig {
        public boolean debug = false;
        public @JsonField("disable_color") boolean disableColor = false;
    }

    @ToString
    @JsonClass(exposeAll = true)
    public static class ServiceConfig {
        public HttpServiceConfig http = new HttpServiceConfig();
        public FtpServiceConfig ftp = new FtpServiceConfig();
        public @JsonField("simple_ui") SimpleUIConfig simpleUI = new SimpleUIConfig();
        public @JsonField("wii_mc") WiiMCServiceConfig wiimc = new WiiMCServiceConfig();

        @ToString
        @JsonClass(exposeAll = true)
        public static class HttpServiceConfig {
            public boolean enable = true;
            public int port = 8125;
        }

        @ToString
        @JsonClass(exposeAll = true)
        public static class FtpServiceConfig {
            public boolean enable = true;
            public int port = 8126;
        }

        @ToString
        @JsonClass(exposeAll = true)
        public static class SimpleUIConfig {
            public boolean enable = true;
            public int port = 8127;
        }

    }

    @ToString
    @JsonClass(exposeAll = true)
    public static class SessionConfig {
    }

    @ToString
    @JsonClass(exposeAll = true)
    public static class TranscodeConfig {
        public TranscodeAcceleration acceleration = TranscodeAcceleration.SOFTWARE_ONLY;

        public static enum TranscodeAcceleration {
            SOFTWARE_ONLY,
            NVIDIA_PREFERRED,
            // TODO AMD & Intel encoders.
            // TODO Implement hardware decoding to speed up the transcode pipeline.
            // https://trac.ffmpeg.org/wiki/HWAccelIntro
        }
    }

}
