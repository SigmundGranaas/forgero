package com.sigmundgranaas.forgero.core.resource.loader;

import com.sigmundgranaas.forgero.core.data.ForgeroDataResource;
import com.sigmundgranaas.forgero.core.resource.PojoFileLoader;
import com.sigmundgranaas.forgero.core.util.JsonPOJOLoader;

import java.nio.file.Path;
import java.util.Optional;

public class PojoFileLoaderImpl<T extends ForgeroDataResource> implements PojoFileLoader<T> {
    private final Class<T> type;

    public PojoFileLoaderImpl(Class<T> type) {
        this.type = type;
    }

    @Override
    public Optional<T> loadFile(Path path) {
        return JsonPOJOLoader.loadPOJO(getFilePath(path), type);
    }

    private String getFilePath(Path path) {
        String[] elements = path.toString().split("/data");
        if (elements.length == 2) {
            return "/data" + elements[1];
        }
        return "";
    }
}
