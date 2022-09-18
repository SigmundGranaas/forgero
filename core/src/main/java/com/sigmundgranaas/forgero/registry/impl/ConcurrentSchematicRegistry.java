package com.sigmundgranaas.forgero.registry.impl;

import com.sigmundgranaas.forgero.registry.SchematicRegistry;
import com.sigmundgranaas.forgero.schematic.Schematic;

import java.util.Map;


public class ConcurrentSchematicRegistry extends ConcurrentResourceRegistry<Schematic> implements SchematicRegistry {
    protected ConcurrentSchematicRegistry(Map<String, Schematic> resources) {
        super(resources);
    }
}
