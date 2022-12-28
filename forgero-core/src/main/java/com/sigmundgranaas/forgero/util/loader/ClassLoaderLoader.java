package com.sigmundgranaas.forgero.util.loader;

import java.io.InputStream;
import java.util.Optional;

public class ClassLoaderLoader implements InputStreamLoader {
    @Override
    public Optional<InputStream> load(String location) {
        return Optional.ofNullable(this.getClass().getClassLoader().getResourceAsStream(location));
    }
}
