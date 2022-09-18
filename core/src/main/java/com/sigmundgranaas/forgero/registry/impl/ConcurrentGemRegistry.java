package com.sigmundgranaas.forgero.registry.impl;

import com.sigmundgranaas.forgero.gem.Gem;
import com.sigmundgranaas.forgero.registry.GemRegistry;

import java.util.Map;

public class ConcurrentGemRegistry extends ConcurrentResourceRegistry<Gem> implements GemRegistry {
    protected ConcurrentGemRegistry(Map<String, Gem> resources) {
        super(resources);
    }
}
