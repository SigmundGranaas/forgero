package com.sigmundgranaas.forgero.core;

import com.sigmundgranaas.forgero.core.data.v1.pojo.*;
import com.sigmundgranaas.forgero.core.gem.Gem;
import com.sigmundgranaas.forgero.core.material.material.ForgeroMaterial;
import com.sigmundgranaas.forgero.core.schematic.Schematic;
import com.sigmundgranaas.forgero.core.tool.ForgeroTool;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPart;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public interface ForgeroResourceLoader {
    @NotNull ResourceLoader<Schematic, SchematicPojo> getSchematicLoader();

    @NotNull
    ResourceLoader<ForgeroTool, ToolPojo> getToolLoader();

    @NotNull
    ResourceLoader<ForgeroToolPart, ToolPartPojo> getToolPartLoader();

    @NotNull
    ResourceLoader<ForgeroMaterial, MaterialPojo> getMaterialLoader();

    @NotNull
    ResourceLoader<Gem, GemPojo> getGemLoader();

    Set<String> getAvailableNameSpaces();
}
