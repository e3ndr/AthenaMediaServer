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

    private boolean enableCudaAcceleration;

    private int port = 8080;

    public File getMediaDirectory() {
        return new File(this.mediaDirectory);
    }

}
