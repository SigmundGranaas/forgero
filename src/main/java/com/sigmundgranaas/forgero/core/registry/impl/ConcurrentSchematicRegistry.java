package com.sigmundgranaas.forgero.core.registry.impl;

import com.sigmundgranaas.forgero.core.registry.SchematicRegistry;
import com.sigmundgranaas.forgero.core.schematic.Schematic;

import java.util.Map;


public class ConcurrentSchematicRegistry extends ConcurrentResourceRegistry<Schematic> implements SchematicRegistry {
    protected ConcurrentSchematicRegistry(Map<String, Schematic> resources) {
        super(resources);
    }
}
