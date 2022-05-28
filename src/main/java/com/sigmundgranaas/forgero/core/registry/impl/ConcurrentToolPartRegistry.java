package com.sigmundgranaas.forgero.core.registry.impl;

import com.sigmundgranaas.forgero.core.registry.ToolPartRegistry;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPart;

import java.util.Map;

public class ConcurrentToolPartRegistry extends ConcurrentResourceRegistry<ForgeroToolPart> implements ToolPartRegistry {
    protected ConcurrentToolPartRegistry(Map<String, ForgeroToolPart> resources) {
        super(resources);
    }
}
