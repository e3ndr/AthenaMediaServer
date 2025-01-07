package xyz.e3ndr.athena.types.media;

import co.casterlabs.rakurai.json.annotating.JsonClass;
import lombok.Getter;
import lombok.NonNull;

@Getter
@NonNull
@JsonClass(exposeAll = true)
public class Media {
    private MediaType type;
    private String id;
    private MediaInfo info;
    private MediaFiles files;

    @Override
    public String toString() {
        if (this.info.getYear() == -1) {
            return String.format("%s [%s]", this.info.getTitle(), this.id);
        } else {
            return String.format("%s (%d) [%s]", this.info.getTitle(), this.info.getYear(), this.id);
        }
    }

}
