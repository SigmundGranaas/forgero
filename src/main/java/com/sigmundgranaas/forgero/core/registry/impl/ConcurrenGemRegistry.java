package com.sigmundgranaas.forgero.core.registry.impl;

import com.sigmundgranaas.forgero.core.gem.Gem;
import com.sigmundgranaas.forgero.core.registry.GemRegistry;

import java.util.Map;

public class ConcurrenGemRegistry extends ConcurrentResourceRegistry<Gem> implements GemRegistry {
    protected ConcurrenGemRegistry(Map<String, Gem> resources) {
        super(resources);
    }
}
