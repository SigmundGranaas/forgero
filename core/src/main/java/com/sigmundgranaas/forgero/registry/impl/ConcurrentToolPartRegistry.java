package com.sigmundgranaas.forgero.registry.impl;

import com.sigmundgranaas.forgero.registry.ToolPartRegistry;
import com.sigmundgranaas.forgero.toolpart.ForgeroToolPart;

import java.util.Map;

public class ConcurrentToolPartRegistry extends ConcurrentResourceRegistry<ForgeroToolPart> implements ToolPartRegistry {
    protected ConcurrentToolPartRegistry(Map<String, ForgeroToolPart> resources) {
        super(resources);
    }
}
