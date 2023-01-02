package com.sigmundgranaas.forgero.core.util.loader;

import java.io.InputStream;
import java.util.Optional;

@FunctionalInterface
public interface InputStreamLoader {
    Optional<InputStream> load(String location);

}
