package com.sigmundgranaas.forgerocore.registry.impl;

import com.sigmundgranaas.forgerocore.gem.Gem;
import com.sigmundgranaas.forgerocore.registry.GemRegistry;

import java.util.Map;

public class ConcurrentGemRegistry extends ConcurrentResourceRegistry<Gem> implements GemRegistry {
    protected ConcurrentGemRegistry(Map<String, Gem> resources) {
        super(resources);
    }
}
