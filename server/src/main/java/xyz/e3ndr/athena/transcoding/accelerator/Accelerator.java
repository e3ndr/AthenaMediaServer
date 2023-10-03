package xyz.e3ndr.athena.transcoding.accelerator;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import lombok.NonNull;
import xyz.e3ndr.athena.types.VideoCodec;
import xyz.e3ndr.athena.types.VideoQuality;

public interface Accelerator {

    public @Nullable List<String> acc_getFF();

    public @Nullable List<String> v_getFF(@NonNull VideoCodec codec, @NonNull VideoQuality quality);

}
