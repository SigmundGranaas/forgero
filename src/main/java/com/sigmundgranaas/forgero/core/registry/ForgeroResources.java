package com.sigmundgranaas.forgero.core.registry;

import com.sigmundgranaas.forgero.core.gem.GemCollection;
import com.sigmundgranaas.forgero.core.material.MaterialCollection;
import com.sigmundgranaas.forgero.core.schematic.SchematicCollection;
import com.sigmundgranaas.forgero.core.tool.ForgeroToolCollection;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPartCollection;

public record ForgeroResources(MaterialCollection materialCollection, GemCollection gemCollection,
                               ForgeroToolCollection toolCollection, ForgeroToolPartCollection toolPartCollection,
                               SchematicCollection schematicCollection) {
}
