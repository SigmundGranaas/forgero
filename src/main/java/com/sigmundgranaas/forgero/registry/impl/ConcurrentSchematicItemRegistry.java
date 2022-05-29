package com.sigmundgranaas.forgero.registry.impl;

import com.sigmundgranaas.forgero.core.registry.impl.ConcurrentResourceRegistry;
import com.sigmundgranaas.forgero.item.items.SchematicItem;

import java.util.HashMap;

public class ConcurrentSchematicItemRegistry extends ConcurrentResourceRegistry<SchematicItem> implements SchematicItemRegistry {
    public ConcurrentSchematicItemRegistry(HashMap<String, SchematicItem> resources) {
        super(resources);
    }
}
