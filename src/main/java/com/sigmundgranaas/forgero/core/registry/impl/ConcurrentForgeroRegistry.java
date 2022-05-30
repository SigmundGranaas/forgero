package com.sigmundgranaas.forgero.core.registry.impl;

import com.sigmundgranaas.forgero.core.ForgeroRegistry;
import com.sigmundgranaas.forgero.core.ForgeroResourceInitializer;
import com.sigmundgranaas.forgero.core.registry.*;

import java.util.HashMap;

/**
 * Concurrent registry for handling all Forgero resources.
 * All registries contained are thread safe
 */
public class ConcurrentForgeroRegistry implements ForgeroRegistry {
    private volatile static ConcurrentForgeroRegistry INSTANCE;
    private final GemRegistry gemRegistry = new ConcurrenGemRegistry(new HashMap<>());
    private final MaterialRegistry materialRegistry = new ConcurrentMaterialRegistry(new HashMap<>());

    private final ToolPartRegistry toolPartRegistry = new ConcurrentToolPartRegistry(new HashMap<>());

    private final ToolRegistry toolRegistry = new ConcurrentToolRegistry(new HashMap<>());

    private final SchematicRegistry schematicRegistry = new ConcurrentSchematicRegistry(new HashMap<>());

    public static ConcurrentForgeroRegistry getInstance() {
        if (INSTANCE == null) {
            synchronized (ConcurrentForgeroRegistry.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ConcurrentForgeroRegistry();
                }
            }
        }
        return INSTANCE;
    }

    @Override
    public MaterialRegistry getMaterialRegistry() {
        return materialRegistry;
    }

    @Override
    public GemRegistry getGemRegistry() {
        return gemRegistry;
    }

    @Override
    public ToolRegistry getToolRegistry() {
        return toolRegistry;
    }

    @Override
    public ToolPartRegistry getToolPartRegistry() {
        return toolPartRegistry;
    }

    @Override
    public SchematicRegistry getSchematicRegistry() {
        return schematicRegistry;
    }

    @Override
    public void clear() {
        materialRegistry.clear();
        gemRegistry.clear();
        toolPartRegistry.clear();
        toolRegistry.clear();
        schematicRegistry.clear();
    }

    @Override
    public boolean isEmpty() {
        return (toolRegistry.isEmpty()
                || toolRegistry.isEmpty()
                || schematicRegistry.isEmpty()
                || getGemRegistry().isEmpty()
                || materialRegistry.isEmpty());
    }

    @Override
    public ConcurrentForgeroRegistry loadResources(ForgeroResourceInitializer initializer) {
        clear();
        var registry = initializer.initializeForgeroResources();
        toolRegistry.updateRegistry(registry.toolCollection());
        toolPartRegistry.updateRegistry(registry.toolPartCollection());
        schematicRegistry.updateRegistry(registry.schematicCollection());
        gemRegistry.updateRegistry(registry.gemCollection());
        materialRegistry.updateRegistry(registry.materialCollection().values().stream().toList());
        return this;
    }

    @Override
    public ConcurrentForgeroRegistry loadResourcesIfEmpty(ForgeroResourceInitializer initializer) {
        if (isEmpty()) {
            loadResources(initializer);
        }
        return this;
    }

    @Override
    public void updateResources() {

    }
}
