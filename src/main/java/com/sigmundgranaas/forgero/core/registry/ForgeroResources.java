package com.sigmundgranaas.forgero.core.registry;

import com.sigmundgranaas.forgero.core.gem.Gem;
import com.sigmundgranaas.forgero.core.material.material.ForgeroMaterial;
import com.sigmundgranaas.forgero.core.schematic.Schematic;
import com.sigmundgranaas.forgero.core.tool.ForgeroTool;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPart;

import java.util.List;
import java.util.Map;

public record ForgeroResources(Map<String, ForgeroMaterial> materialCollection, List<Gem> gemCollection,
                               List<ForgeroTool> toolCollection, List<ForgeroToolPart> toolPartCollection,
                               List<Schematic> schematicCollection) {
}
