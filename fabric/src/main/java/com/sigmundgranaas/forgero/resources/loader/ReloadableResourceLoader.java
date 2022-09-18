package com.sigmundgranaas.forgero.resources.loader;

import com.sigmundgranaas.forgero.resource.data.v1.ForgeroDataResource;
import com.sigmundgranaas.forgero.resource.ForgeroResourceType;
import com.sigmundgranaas.forgero.resource.InputStreamProvider;
import com.sigmundgranaas.forgero.resource.PojoLoader;
import com.sigmundgranaas.forgero.resource.loader.PojoInputStreamLoader;

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
