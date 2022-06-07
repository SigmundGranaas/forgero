package com.sigmundgranaas.forgero.core.registry.impl;

import com.sigmundgranaas.forgero.core.gem.Gem;
import com.sigmundgranaas.forgero.core.registry.GemRegistry;

import java.util.Map;

public class ConcurrentGemRegistry extends ConcurrentResourceRegistry<Gem> implements GemRegistry {
    protected ConcurrentGemRegistry(Map<String, Gem> resources) {
        super(resources);
    }
}
