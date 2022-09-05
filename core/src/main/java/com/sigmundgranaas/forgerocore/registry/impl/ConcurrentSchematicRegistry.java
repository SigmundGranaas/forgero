package com.sigmundgranaas.forgerocore.registry.impl;

import com.sigmundgranaas.forgerocore.registry.SchematicRegistry;
import com.sigmundgranaas.forgerocore.schematic.Schematic;

import java.util.Map;


public class ConcurrentSchematicRegistry extends ConcurrentResourceRegistry<Schematic> implements SchematicRegistry {
    protected ConcurrentSchematicRegistry(Map<String, Schematic> resources) {
        super(resources);
    }
}
