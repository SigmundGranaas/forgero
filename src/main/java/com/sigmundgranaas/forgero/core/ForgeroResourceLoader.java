package com.sigmundgranaas.forgero.core;

import com.sigmundgranaas.forgero.core.data.v1.pojo.*;
import com.sigmundgranaas.forgero.core.gem.Gem;
import com.sigmundgranaas.forgero.core.material.material.ForgeroMaterial;
import com.sigmundgranaas.forgero.core.schematic.Schematic;
import com.sigmundgranaas.forgero.core.tool.ForgeroTool;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPart;

public interface ForgeroResourceLoader {
    ResourceLoader<Schematic, SchematicPojo> getSchematicLoader();

    ResourceLoader<ForgeroTool, ToolPojo> getToolLoader();

    ResourceLoader<ForgeroToolPart, ToolPartPojo> getToolPartLoader();

    ResourceLoader<ForgeroMaterial, MaterialPojo> getMaterialLoader();

    ResourceLoader<Gem, GemPojo> getGemLoader();
}
