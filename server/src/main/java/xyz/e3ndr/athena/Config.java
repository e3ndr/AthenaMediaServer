package xyz.e3ndr.athena;

import java.io.File;

import co.casterlabs.rakurai.json.annotating.JsonClass;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@JsonClass(exposeAll = true)
public class Config {
    private boolean debug = false;
    private boolean disableColoredConsole = false;

    private String mediaDirectory = "./Media";
    private String cacheDirectory = "./Cache";
    private String ingestDirectory = "./Ingest";

    private boolean enableCudaAcceleration;

    // -1 to disable.
    private int httpPort = 8125;
    private int ftpPort = 8126;

    public File getMediaDirectory() {
        return new File(this.mediaDirectory);
    }

    public File getCacheDirectory() {
        return new File(this.cacheDirectory);
    }

    public File getIngestDirectory() {
        return new File(this.ingestDirectory);
    }

}
