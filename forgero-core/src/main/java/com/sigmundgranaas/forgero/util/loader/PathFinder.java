package com.sigmundgranaas.forgero.util.loader;

import com.sigmundgranaas.forgero.Forgero;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Optional;

@FunctionalInterface
public interface PathFinder {
    static Optional<Path> ClassLoaderFinder(String path) {
        Optional<URL> url = Optional.ofNullable(PathFinder.class.getClassLoader().getResource(path));
        return url.flatMap(PathFinder::uriConverter).map(Path::of);
    }

    private static Optional<URI> uriConverter(URL url) {
        try {
            return Optional.of(url.toURI());
        } catch (URISyntaxException exception) {
            Forgero.LOGGER.error("Unable to convert url: {} to URI", url);
            Forgero.LOGGER.error(exception);
            return Optional.empty();
        }
    }

    Optional<Path> find(String location);
}
