package xyz.e3ndr.athena;

import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.annotating.JsonField;
import lombok.ToString;
import xyz.e3ndr.athena.transcoding.accelerator.TranscodeAcceleration;

@ToString
@JsonClass(exposeAll = true)
public class Config {
    public ConsoleConfig console = new ConsoleConfig();
    public ServiceConfig services = new ServiceConfig();
    public SessionConfig sessions = new SessionConfig();
    public TranscodeConfig transcoding = new TranscodeConfig();

    @JsonField("media_directory")
    public String mediaDirectory = "./Media";

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
        public @JsonField("special") SpecialServiceConfig special = new SpecialServiceConfig();

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

        @ToString
        @JsonClass(exposeAll = true)
        public static class SpecialServiceConfig {
            public boolean enable = false;
            public int port = 8128;
        }

    }

    @ToString
    @JsonClass(exposeAll = true)
    public static class SessionConfig {
    }

    @ToString
    @JsonClass(exposeAll = true)
    public static class TranscodeConfig {
        public boolean enable = true;
        public TranscodeAcceleration acceleration = TranscodeAcceleration.SOFTWARE_ONLY;
        public @JsonField("cache_rentention_h") int cacheRetentionHours = 36;
    }

}
