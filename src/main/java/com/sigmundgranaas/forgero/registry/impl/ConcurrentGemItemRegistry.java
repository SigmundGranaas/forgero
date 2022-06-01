package com.sigmundgranaas.forgero.registry.impl;

import com.google.common.collect.ImmutableList;
import com.sigmundgranaas.forgero.core.data.v1.pojo.GemPojo;
import com.sigmundgranaas.forgero.core.property.PropertyStream;
import com.sigmundgranaas.forgero.item.items.GemItem;
import com.sigmundgranaas.forgero.registry.GemItemRegistry;

import java.util.HashMap;

public class ConcurrentGemItemRegistry extends ConcurrentItemResourceRegistry<GemItem, GemPojo> implements GemItemRegistry {
    public ConcurrentGemItemRegistry(HashMap<String, GemItem> resources) {
        super(resources);
    }

    @Override
    public ImmutableList<GemItem> getResourcesAsList() {
        return PropertyStream.sortedByRarity(super.getResourcesAsList());
    }
}
