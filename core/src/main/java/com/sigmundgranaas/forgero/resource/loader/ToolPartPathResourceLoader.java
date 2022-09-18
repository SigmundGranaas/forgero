package com.sigmundgranaas.forgero.resource.loader;

import com.google.common.collect.ImmutableList;
import com.sigmundgranaas.forgero.resource.data.v1.pojo.ToolPartPojo;
import com.sigmundgranaas.forgero.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.resource.FactoryProvider;
import com.sigmundgranaas.forgero.resource.ForgeroResourceType;
import com.sigmundgranaas.forgero.resource.PojoLoader;
import com.sigmundgranaas.forgero.schematic.Schematic;
import com.sigmundgranaas.forgero.toolpart.ForgeroToolPart;
import com.sigmundgranaas.forgero.toolpart.factory.ForgeroToolPartFactoryImpl;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class ToolPartPathResourceLoader extends ResourceLoaderImpl<ForgeroToolPart, ToolPartPojo> {
    private final List<PrimaryMaterial> materials;
    private final List<Schematic> schematics;

    public ToolPartPathResourceLoader(PojoLoader<ToolPartPojo> pojoLoader, Consumer<ToolPartPojo> handler, FactoryProvider<ForgeroToolPart, ToolPartPojo> factory, ForgeroResourceType type, List<PrimaryMaterial> materials, List<Schematic> schematics) {
        super(pojoLoader, handler, factory, type);
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