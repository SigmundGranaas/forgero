package com.sigmundgranaas.forgerocore.registry.impl;

import com.sigmundgranaas.forgerocore.registry.ToolPartRegistry;
import com.sigmundgranaas.forgerocore.toolpart.ForgeroToolPart;

import java.util.Map;

public class ConcurrentToolPartRegistry extends ConcurrentResourceRegistry<ForgeroToolPart> implements ToolPartRegistry {
    protected ConcurrentToolPartRegistry(Map<String, ForgeroToolPart> resources) {
        super(resources);
    }
}
