package com.sigmundgranaas.forgero.texture.V2;

import java.io.InputStream;
import java.util.Optional;

public interface FileLoader {
    Optional<InputStream> getStream(String location);
}
