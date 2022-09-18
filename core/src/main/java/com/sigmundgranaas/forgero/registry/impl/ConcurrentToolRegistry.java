package com.sigmundgranaas.forgero.registry.impl;

import com.sigmundgranaas.forgero.registry.ToolRegistry;
import com.sigmundgranaas.forgero.tool.ForgeroTool;

import java.util.Map;

public class ConcurrentToolRegistry extends ConcurrentResourceRegistry<ForgeroTool> implements ToolRegistry {
    protected ConcurrentToolRegistry(Map<String, ForgeroTool> resources) {
        super(resources);
    }
}
