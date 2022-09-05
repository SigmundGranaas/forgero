package com.sigmundgranaas.forgero.registry.impl;

import com.sigmundgranaas.forgerocore.data.v1.pojo.SchematicPojo;
import com.sigmundgranaas.forgero.item.items.SchematicItem;
import com.sigmundgranaas.forgero.registry.SchematicItemRegistry;

import java.util.Map;

public class ConcurrentSchematicItemRegistry extends ConcurrentItemResourceRegistry<SchematicItem, SchematicPojo> implements SchematicItemRegistry {
    public ConcurrentSchematicItemRegistry(Map<String, SchematicItem> resources) {
        super(resources);
    }
}
