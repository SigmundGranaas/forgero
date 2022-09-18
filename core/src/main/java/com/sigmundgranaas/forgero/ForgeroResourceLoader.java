package com.sigmundgranaas.forgero;

import com.sigmundgranaas.forgero.gem.Gem;
import com.sigmundgranaas.forgero.material.material.ForgeroMaterial;
import com.sigmundgranaas.forgero.resource.data.v1.pojo.*;
import com.sigmundgranaas.forgero.schematic.Schematic;
import com.sigmundgranaas.forgero.tool.ForgeroTool;
import com.sigmundgranaas.forgero.toolpart.ForgeroToolPart;
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
