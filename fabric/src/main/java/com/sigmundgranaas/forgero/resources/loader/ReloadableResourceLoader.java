package com.sigmundgranaas.forgero.resources.loader;

import com.sigmundgranaas.forgerocore.data.v1.ForgeroDataResource;
import com.sigmundgranaas.forgerocore.resource.ForgeroResourceType;
import com.sigmundgranaas.forgerocore.resource.InputStreamProvider;
import com.sigmundgranaas.forgerocore.resource.PojoLoader;
import com.sigmundgranaas.forgerocore.resource.loader.PojoInputStreamLoader;

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
