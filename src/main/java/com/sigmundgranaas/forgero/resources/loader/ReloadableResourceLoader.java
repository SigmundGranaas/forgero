package com.sigmundgranaas.forgero.resources.loader;

import com.sigmundgranaas.forgero.core.data.ForgeroDataResource;
import com.sigmundgranaas.forgero.core.resource.ForgeroResourceType;
import com.sigmundgranaas.forgero.core.resource.InputStreamProvider;
import com.sigmundgranaas.forgero.core.resource.PojoLoader;
import com.sigmundgranaas.forgero.core.resource.loader.PojoInputStreamLoader;

import java.util.Set;

public class ReloadableResourceLoader extends FabricResourceLoader {
    private final InputStreamProvider streamProvider;

    public ReloadableResourceLoader(Set<String> nameSpaces, InputStreamProvider streamProvider) {
        super(nameSpaces);
        this.streamProvider = streamProvider;
    }

    @Override
    protected <R extends ForgeroDataResource> PojoLoader<R> createPojoLoader(Class<R> classType, ForgeroResourceType type) {
        return new PojoInputStreamLoader<>(streamProvider.getStreams(type), classType);
    }
}
