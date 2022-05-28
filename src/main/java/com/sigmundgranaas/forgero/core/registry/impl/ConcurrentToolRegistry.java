package com.sigmundgranaas.forgero.core.registry.impl;

import com.sigmundgranaas.forgero.core.registry.ToolRegistry;
import com.sigmundgranaas.forgero.core.tool.ForgeroTool;

import java.util.Map;

public class ConcurrentToolRegistry extends ConcurrentResourceRegistry<ForgeroTool> implements ToolRegistry {
    protected ConcurrentToolRegistry(Map<String, ForgeroTool> resources) {
        super(resources);
    }
}
