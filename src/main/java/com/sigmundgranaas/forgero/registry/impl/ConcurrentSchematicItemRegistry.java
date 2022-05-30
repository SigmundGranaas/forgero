package com.sigmundgranaas.forgero.registry.impl;

import com.sigmundgranaas.forgero.core.registry.impl.ConcurrentResourceRegistry;
import com.sigmundgranaas.forgero.item.items.SchematicItem;

import java.util.Map;

public class ConcurrentSchematicItemRegistry extends ConcurrentResourceRegistry<SchematicItem> implements SchematicItemRegistry {
    public ConcurrentSchematicItemRegistry(Map<String, SchematicItem> resources) {
        super(resources);
    }
}
