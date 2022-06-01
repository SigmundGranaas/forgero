package com.sigmundgranaas.forgero.core.resource.loader;

import com.google.common.collect.ImmutableList;
import com.sigmundgranaas.forgero.core.data.v1.pojo.ToolPartPojo;
import com.sigmundgranaas.forgero.core.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.core.resource.ForgeroResourceFactory;
import com.sigmundgranaas.forgero.core.resource.ForgeroResourceType;
import com.sigmundgranaas.forgero.core.resource.ResourcePathProvider;
import com.sigmundgranaas.forgero.core.schematic.Schematic;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPart;
import com.sigmundgranaas.forgero.core.toolpart.factory.ForgeroToolPartFactoryImpl;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

public class ToolPartPathResourceLoader extends PathResourceLoader<ForgeroToolPart, ToolPartPojo> {
    private final List<PrimaryMaterial> materials;
    private final List<Schematic> schematics;

    public ToolPartPathResourceLoader(ResourcePathProvider pathProvider, Consumer<ToolPartPojo> handler, Function<List<ToolPartPojo>, ForgeroResourceFactory<ForgeroToolPart, ToolPartPojo>> factory, PojoFileLoaderImpl<ToolPartPojo> fileLoader, ForgeroResourceType type, List<PrimaryMaterial> materials, List<Schematic> schematics) {
        super(pathProvider, handler, factory, fileLoader, type);
        this.materials = materials;
        this.schematics = schematics;
    }

    @Override
    public @NotNull ImmutableList<ForgeroToolPart> loadResources() {
        var toolPartsFromFile = super.loadResources();

        var toolPartFromFactory = new ForgeroToolPartFactoryImpl().createBaseToolParts(materials, schematics);

        return Stream.of(toolPartFromFactory, toolPartsFromFile).flatMap(List::stream).collect(ImmutableList.toImmutableList());
    }
}