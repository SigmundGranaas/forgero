package com.sigmundgranaas.forgero.resource.loader;

import com.sigmundgranaas.forgero.resource.data.v1.ForgeroDataResource;
import com.sigmundgranaas.forgero.resource.PojoLoader;
import com.sigmundgranaas.forgero.util.JsonPOJOLoader;

import java.io.InputStream;
import java.util.Collection;
import java.util.Optional;

public class PojoInputStreamLoader<T extends ForgeroDataResource> implements PojoLoader<T> {

    private final Collection<InputStream> streams;
    private final Class<T> type;

    public PojoInputStreamLoader(Collection<InputStream> streams, Class<T> type) {
        this.streams = streams;
        this.type = type;
    }

    @Override
    public Collection<T> loadPojos() {
        return streams.stream().map(stream -> JsonPOJOLoader.loadPOJO(stream, type)).flatMap(Optional::stream).toList();
    }
}
