package com.sigmundgranaas.forgerocore;

import com.sigmundgranaas.forgerocore.data.v1.pojo.*;
import com.sigmundgranaas.forgerocore.gem.Gem;
import com.sigmundgranaas.forgerocore.material.material.ForgeroMaterial;
import com.sigmundgranaas.forgerocore.schematic.Schematic;
import com.sigmundgranaas.forgerocore.tool.ForgeroTool;
import com.sigmundgranaas.forgerocore.toolpart.ForgeroToolPart;
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
