package com.sigmundgranaas.forgerocore.registry.impl;

import com.sigmundgranaas.forgerocore.registry.ToolRegistry;
import com.sigmundgranaas.forgerocore.tool.ForgeroTool;

import java.util.Map;

public class ConcurrentToolRegistry extends ConcurrentResourceRegistry<ForgeroTool> implements ToolRegistry {
    protected ConcurrentToolRegistry(Map<String, ForgeroTool> resources) {
        super(resources);
    }
}
