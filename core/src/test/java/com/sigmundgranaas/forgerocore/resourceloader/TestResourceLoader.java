package com.sigmundgranaas.forgerocore.resourceloader;

import com.google.common.collect.ImmutableList;
import com.sigmundgranaas.forgerocore.ForgeroResourceLoader;
import com.sigmundgranaas.forgerocore.ResourceLoader;
import com.sigmundgranaas.forgerocore.data.v1.pojo.*;
import com.sigmundgranaas.forgerocore.gem.Gem;
import com.sigmundgranaas.forgerocore.gem.implementation.FileGemLoader;
import com.sigmundgranaas.forgerocore.material.implementation.SimpleMaterialLoader;
import com.sigmundgranaas.forgerocore.material.material.ForgeroMaterial;
import com.sigmundgranaas.forgerocore.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgerocore.schematic.Schematic;
import com.sigmundgranaas.forgerocore.schematic.SchematicLoader;
import com.sigmundgranaas.forgerocore.tool.ForgeroTool;
import com.sigmundgranaas.forgerocore.tool.factory.ForgeroToolFactoryImpl;
import com.sigmundgranaas.forgerocore.toolpart.ForgeroToolPart;
import com.sigmundgranaas.forgerocore.toolpart.factory.ForgeroToolPartFactory;
import com.sigmundgranaas.forgerocore.toolpart.factory.ForgeroToolPartFactoryImpl;
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
