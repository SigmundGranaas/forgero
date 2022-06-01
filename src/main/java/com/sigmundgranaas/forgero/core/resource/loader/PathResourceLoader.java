package com.sigmundgranaas.forgero.core.resource.loader;

import com.google.common.collect.ImmutableList;
import com.sigmundgranaas.forgero.core.ResourceLoader;
import com.sigmundgranaas.forgero.core.data.ForgeroDataResource;
import com.sigmundgranaas.forgero.core.resource.ForgeroResource;
import com.sigmundgranaas.forgero.core.resource.ForgeroResourceFactory;
import com.sigmundgranaas.forgero.core.resource.ForgeroResourceType;
import com.sigmundgranaas.forgero.core.resource.ResourcePathProvider;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public class PathResourceLoader<T extends ForgeroResource<R>, R extends ForgeroDataResource> implements ResourceLoader<T, R> {
    protected final ResourcePathProvider pathProvider;
    protected final Consumer<R> handler;
    protected final Function<List<R>, ForgeroResourceFactory<T, R>> factoryProvider;
    protected final PojoFileLoaderImpl<R> fileLoader;
    protected final ForgeroResourceType type;

    private ImmutableList<T> resources = new ImmutableList.Builder<T>().build();

    public PathResourceLoader(ResourcePathProvider pathProvider, Consumer<R> handler, Function<List<R>, ForgeroResourceFactory<T, R>> factory, PojoFileLoaderImpl<R> fileLoader, ForgeroResourceType type) {
        this.pathProvider = pathProvider;
        this.handler = handler;
        this.factoryProvider = factory;
        this.fileLoader = fileLoader;
        this.type = type;
    }

    public PathResourceLoader(ResourcePathProvider pathProvider, Function<List<R>, ForgeroResourceFactory<T, R>> factory, PojoFileLoaderImpl<R> fileLoader, ForgeroResourceType type) {
        this.pathProvider = pathProvider;
        this.handler = (none) -> {
        };
        this.factoryProvider = factory;
        this.fileLoader = fileLoader;
        this.type = type;
    }

    @Override
    public @NotNull ImmutableList<T> loadResources() {
        if (resources.isEmpty()) {
            var pojos = pathProvider.getPaths(type).stream()
                    .map(fileLoader::loadFile)
                    .flatMap(Optional::stream)
                    .toList();

            pojos.forEach(handler);
            resources = factoryProvider.apply(pojos).createResources();
        }
        return resources;
    }
}
