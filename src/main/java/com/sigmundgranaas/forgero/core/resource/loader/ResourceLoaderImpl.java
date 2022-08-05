package com.sigmundgranaas.forgero.core.resource.loader;

import com.google.common.collect.ImmutableList;
import com.sigmundgranaas.forgero.core.ResourceLoader;
import com.sigmundgranaas.forgero.core.data.v1.ForgeroDataResource;
import com.sigmundgranaas.forgero.core.resource.FactoryProvider;
import com.sigmundgranaas.forgero.core.resource.ForgeroResource;
import com.sigmundgranaas.forgero.core.resource.ForgeroResourceType;
import com.sigmundgranaas.forgero.core.resource.PojoLoader;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class ResourceLoaderImpl<T extends ForgeroResource<R>, R extends ForgeroDataResource> implements ResourceLoader<T, R> {
    protected final PojoLoader<R> pojoLoader;
    protected final Consumer<R> handler;
    protected final FactoryProvider<T, R> factoryProvider;
    protected final ForgeroResourceType type;
    protected ImmutableList<R> pojos;

    private ImmutableList<T> resources = new ImmutableList.Builder<T>().build();

    public ResourceLoaderImpl(PojoLoader<R> pojoLoader, Consumer<R> handler, FactoryProvider<T, R> factoryProvider, ForgeroResourceType type) {
        this.pojoLoader = pojoLoader;
        this.handler = handler;
        this.factoryProvider = factoryProvider;
        this.type = type;
    }

    public ResourceLoaderImpl(PojoLoader<R> pojoLoader, FactoryProvider<T, R> factoryProvider, ForgeroResourceType type) {
        this.pojoLoader = pojoLoader;
        this.handler = (none) -> {
        };
        this.factoryProvider = factoryProvider;

        this.type = type;
    }

    @Override
    public @NotNull ImmutableList<T> loadResources() {
        if (resources.isEmpty()) {
            pojos = pojoLoader.loadPojos().stream().collect(ImmutableList.toImmutableList());

            pojos.forEach(handler);

            resources = factoryProvider.createFactory(pojos).createResources();
        }
        return resources;
    }

    @Override
    public @NotNull ImmutableList<R> loadPojos() {
        if (pojos == null) {
            loadResources();
        }
        return pojos;
    }
}
