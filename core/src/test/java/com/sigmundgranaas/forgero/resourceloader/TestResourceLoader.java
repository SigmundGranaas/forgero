package com.sigmundgranaas.forgero.resourceloader;

import com.google.common.collect.ImmutableList;
import com.sigmundgranaas.forgero.ForgeroResourceLoader;
import com.sigmundgranaas.forgero.ResourceLoader;
import com.sigmundgranaas.forgero.gem.Gem;
import com.sigmundgranaas.forgero.gem.implementation.FileGemLoader;
import com.sigmundgranaas.forgero.material.implementation.SimpleMaterialLoader;
import com.sigmundgranaas.forgero.material.material.ForgeroMaterial;
import com.sigmundgranaas.forgero.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.resource.data.v1.pojo.*;
import com.sigmundgranaas.forgero.schematic.Schematic;
import com.sigmundgranaas.forgero.schematic.SchematicLoader;
import com.sigmundgranaas.forgero.tool.ForgeroTool;
import com.sigmundgranaas.forgero.tool.factory.ForgeroToolFactoryImpl;
import com.sigmundgranaas.forgero.toolpart.ForgeroToolPart;
import com.sigmundgranaas.forgero.toolpart.factory.ForgeroToolPartFactory;
import com.sigmundgranaas.forgero.toolpart.factory.ForgeroToolPartFactoryImpl;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

public class TestResourceLoader implements ForgeroResourceLoader {


    @Override
    public @NotNull ResourceLoader<Schematic, SchematicPojo> getSchematicLoader() {
        return () -> new SchematicLoader().loadSchematics().stream().collect(ImmutableList.toImmutableList());
    }

    @Override
    public @NotNull ResourceLoader<ForgeroTool, ToolPojo> getToolLoader() {
        return () -> new ForgeroToolFactoryImpl().createForgeroTools(getToolPartLoader().loadResources()).stream().collect(ImmutableList.toImmutableList());
    }

    @Override
    public @NotNull ResourceLoader<ForgeroToolPart, ToolPartPojo> getToolPartLoader() {
        ForgeroToolPartFactory factory = new ForgeroToolPartFactoryImpl();

        return () -> factory.createBaseToolParts(
                        getMaterialLoader().loadResources().stream().filter(material -> material instanceof PrimaryMaterial).map(PrimaryMaterial.class::cast).toList(),
                        getSchematicLoader().loadResources().stream().toList())
                .stream().collect(ImmutableList.toImmutableList());

    }

    @Override
    public @NotNull ResourceLoader<ForgeroMaterial, MaterialPojo> getMaterialLoader() {
        return () -> new SimpleMaterialLoader(List.of("iron", "oak", "diamond", "netherite")).getMaterials().values().stream().collect(ImmutableList.toImmutableList());
    }

    @Override
    public @NotNull ResourceLoader<Gem, GemPojo> getGemLoader() {
        return () -> new FileGemLoader(List.of("diamond", "emerald", "lapis")).loadGems().stream().collect(ImmutableList.toImmutableList());
    }

    @Override
    public Set<String> getAvailableNameSpaces() {
        return Set.of("forgero, minecraft");
    }
}
