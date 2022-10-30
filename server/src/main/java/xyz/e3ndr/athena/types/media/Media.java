package xyz.e3ndr.athena.types.media;

import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import co.casterlabs.commons.functional.tuples.Pair;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import lombok.Getter;
import lombok.NonNull;
import xyz.e3ndr.athena.Athena;

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

    public File getStreamFile(int streamId) {
        return new File(
            Athena.mediaDirectory,
            String.format("%s/streams/%d.%s", this.id, streamId, Athena.STREAM_FORMAT)
        );
    }

    /**
     * a: The main subtitle file. (nullable)<br />
     * b: Any forced subtitles.
     */
    public @NonNull Pair<File, List<File>> getSubtitle(String language) {
        for (MediaFiles.Subtitle subtitle : this.files.getSubtitles()) {
            if (!subtitle.getLanguage().equals(language)) continue;

            File mainSubtitle = new File(
                Athena.mediaDirectory,
                String.format("%s/subtitles/%s", this.id, subtitle.getFile())
            );

            List<File> forcedSubtitles = new LinkedList<>();
            for (String forced : subtitle.getForced()) {
                forcedSubtitles.add(
                    new File(
                        Athena.mediaDirectory,
                        String.format("%s/subtitles/%s", this.id, forced)
                    )
                );
            }

            return new Pair<>(mainSubtitle, forcedSubtitles);
        }

        return new Pair<>(null, Collections.emptyList());
    }

}
