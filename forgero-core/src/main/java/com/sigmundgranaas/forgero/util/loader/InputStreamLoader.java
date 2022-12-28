package com.sigmundgranaas.forgero.util.loader;

import java.io.InputStream;
import java.util.Optional;

@FunctionalInterface
public interface InputStreamLoader {
    Optional<InputStream> load(String location);

}
