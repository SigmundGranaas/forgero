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
import com.sigmundgranaas.forgero.core.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.core.resource.ForgeroResource;
import com.sigmundgranaas.forgero.core.resource.ForgeroResourceFactory;
import com.sigmundgranaas.forgero.core.resource.ForgeroResourceType;
import com.sigmundgranaas.forgero.core.resource.loader.PathResourceLoader;
import com.sigmundgranaas.forgero.core.resource.loader.PojoFileLoaderImpl;
import com.sigmundgranaas.forgero.core.schematic.Schematic;
import com.sigmundgranaas.forgero.core.tool.ForgeroTool;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPart;
import com.sigmundgranaas.forgero.core.toolpart.factory.ForgeroToolPartFactoryImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class FabricResourceLoader implements ForgeroResourceLoader {
    private final Set<String> availableNameSpaces;

    private List<PrimaryMaterial> materials;
    private List<Schematic> schematics;
    private List<ForgeroToolPart> toolParts;

    public FabricResourceLoader(Set<String> availableNameSpaces) {
        this.materials = new ArrayList<>();
        this.schematics = new ArrayList<>();
        this.toolParts = new ArrayList<>();
        this.availableNameSpaces = availableNameSpaces;
    }

    @Override
    public ResourceLoader<Schematic, SchematicPojo> getSchematicLoader() {

        var loader = createResourceLoader(
                SchematicPojo.class,
                ForgeroResourceType.SCHEMATIC,
                (namespaces, pojos) -> new SchematicFactory(pojos, namespaces));
        schematics = loader.loadResources();
        return loader;
    }

    @Override
    public ResourceLoader<ForgeroTool, ToolPojo> getToolLoader() {
        return null;
    }

    @Override
    public ResourceLoader<ForgeroToolPart, ToolPartPojo> getToolPartLoader() {
        var fileLoader = new PojoFileLoaderImpl<>(ToolPartPojo.class);
        var loader = new ToolPartPathResourceLoader(
                FabricPathProvider.PROVIDER,
                (pojo) -> {
                },
                (pojo) -> new ForgeroToolPartFactoryImpl().createResource(pojo),
                fileLoader,
                ForgeroResourceType.TOOL_PART,
                materials,
                schematics);
        toolParts = loader.loadResources();
        return loader;
    }

    @Override
    public ResourceLoader<ForgeroMaterial, MaterialPojo> getMaterialLoader() {
        var loader = createResourceLoader(
                MaterialPojo.class,
                ForgeroResourceType.MATERIAL,
                (namespaces, pojos) -> new MaterialFactoryImpl(pojos, namespaces));
        this.materials = loader.loadResources().stream().filter(PrimaryMaterial.class::isInstance).map(PrimaryMaterial.class::cast).toList();

        return loader;
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

    private <T extends ForgeroResource<R>, R extends ForgeroDataResource> ResourceLoader<T, R> createResourceLoader(Class<R> classType, Consumer<R> handler, ForgeroResourceType type, BiFunction<Set<String>, List<R>, ForgeroResourceFactory<T, R>> factoryCreator) {
        var fileLoader = new PojoFileLoaderImpl<>(classType);
        Function<List<R>, ForgeroResourceFactory<T, R>> factoryProvider = (List<R> list) -> factoryCreator.apply(availableNameSpaces, list);

        return new PathResourceLoader<>(
                FabricPathProvider.PROVIDER,
                handler,
                factoryProvider,
                fileLoader,
                type
        );
    }
}
