package com.sigmundgranaas.forgero.resources;

import com.sigmundgranaas.forgero.core.ForgeroResourceLoader;
import com.sigmundgranaas.forgero.core.ResourceLoader;
import com.sigmundgranaas.forgero.core.data.ForgeroDataResource;
import com.sigmundgranaas.forgero.core.data.factory.GemFactory;
import com.sigmundgranaas.forgero.core.data.factory.MaterialFactoryImpl;
import com.sigmundgranaas.forgero.core.data.factory.SchematicFactory;
import com.sigmundgranaas.forgero.core.data.v1.pojo.*;
import com.sigmundgranaas.forgero.core.gem.Gem;
import com.sigmundgranaas.forgero.core.material.material.ForgeroMaterial;
import com.sigmundgranaas.forgero.core.resource.ForgeroResource;
import com.sigmundgranaas.forgero.core.resource.ForgeroResourceFactory;
import com.sigmundgranaas.forgero.core.resource.ForgeroResourceType;
import com.sigmundgranaas.forgero.core.resource.loader.PathResourceLoader;
import com.sigmundgranaas.forgero.core.resource.loader.PojoFileLoaderImpl;
import com.sigmundgranaas.forgero.core.schematic.Schematic;
import com.sigmundgranaas.forgero.core.tool.ForgeroTool;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPart;

import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

public class FabricResourceLoader implements ForgeroResourceLoader {
    private final Set<String> availableNameSpaces;

    public FabricResourceLoader(Set<String> availableNameSpaces) {
        this.availableNameSpaces = availableNameSpaces;
    }

    @Override
    public ResourceLoader<Schematic, SchematicPojo> getSchematicLoader() {
        return createResourceLoader(
                SchematicPojo.class,
                ForgeroResourceType.SCHEMATIC,
                (namespaces, pojos) -> new SchematicFactory(pojos, namespaces));
    }

    @Override
    public ResourceLoader<ForgeroTool, ToolPojo> getToolLoader() {
        return null;
    }

    @Override
    public ResourceLoader<ForgeroToolPart, ToolPartPojo> getToolPartLoader() {
        return null;
    }

    @Override
    public ResourceLoader<ForgeroMaterial, MaterialPojo> getMaterialLoader() {
        return createResourceLoader(
                MaterialPojo.class,
                ForgeroResourceType.MATERIAL,
                (namespaces, pojos) -> new MaterialFactoryImpl(pojos, namespaces));
    }

    @Override
    public ResourceLoader<Gem, GemPojo> getGemLoader() {
        return createResourceLoader(
                GemPojo.class,
                ForgeroResourceType.GEM,
                (namespaces, pojos) -> new GemFactory(pojos, namespaces));
    }


    private <T extends ForgeroResource<R>, R extends ForgeroDataResource> ResourceLoader<T, R> createResourceLoader(Class<R> classType, ForgeroResourceType type, BiFunction<Set<String>, List<R>, ForgeroResourceFactory<T, R>> factoryCreator) {
        var fileLoader = new PojoFileLoaderImpl<>(classType);
        Function<List<R>, ForgeroResourceFactory<T, R>> factoryProvider = (List<R> list) -> factoryCreator.apply(availableNameSpaces, list);

        return new PathResourceLoader<>(
                FabricPathProvider.PROVIDER,
                factoryProvider,
                fileLoader,
                type
        );
    }
}
