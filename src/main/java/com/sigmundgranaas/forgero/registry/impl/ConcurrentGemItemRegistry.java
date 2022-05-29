package com.sigmundgranaas.forgero.registry.impl;

import com.sigmundgranaas.forgero.core.registry.impl.ConcurrentResourceRegistry;
import com.sigmundgranaas.forgero.item.items.GemItem;

import java.util.HashMap;

public class ConcurrentGemItemRegistry extends ConcurrentResourceRegistry<GemItem> implements GemItemRegistry {
    public ConcurrentGemItemRegistry(HashMap<String, GemItem> resources) {
        super(resources);
    }
}
