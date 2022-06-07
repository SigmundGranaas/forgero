package com.sigmundgranaas.forgero.core.resource.loader;

import com.sigmundgranaas.forgero.core.data.ForgeroDataResource;
import com.sigmundgranaas.forgero.core.resource.PojoLoader;
import com.sigmundgranaas.forgero.core.util.JsonPOJOLoader;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;

public class PojoFileLoaderImpl<T extends ForgeroDataResource> implements PojoLoader<T> {
    private final Collection<Path> paths;
    private final Class<T> type;

    public PojoFileLoaderImpl(Collection<Path> paths, Class<T> type) {
        this.paths = paths;
        this.type = type;
    }

    private String getFilePath(Path path) {
        String[] elements = path.toString().split("/data");
        if (elements.length == 2) {
            return "/data" + elements[1];
        }
        return "";
    }

    @Override
    public Collection<T> loadPojos() {
        return paths.stream().map(path -> JsonPOJOLoader.loadPOJO(getFilePath(path), type)).flatMap(Optional::stream).toList();
    }
}
