package com.sigmundgranaas.forgerocore.registry.impl;

import com.sigmundgranaas.forgerocore.ForgeroRegistry;
import com.sigmundgranaas.forgerocore.ForgeroResourceLoader;
import com.sigmundgranaas.forgerocore.registry.*;

import java.util.HashMap;

/**
 * Concurrent registry for handling all Forgero resources.
 * All registries contained are thread safe
 */
public class ConcurrentForgeroRegistry implements ForgeroRegistry {
    private volatile static ConcurrentForgeroRegistry INSTANCE;
    private final GemRegistry gemRegistry = new ConcurrentGemRegistry(new HashMap<>());
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
                || gemRegistry.isEmpty()
                || materialRegistry.isEmpty());
    }

    @Override
    public ConcurrentForgeroRegistry loadResources(ForgeroResourceLoader loader) {
        materialRegistry.replaceRegistry(loader.getMaterialLoader().loadResources());
        schematicRegistry.replaceRegistry(loader.getSchematicLoader().loadResources());
        gemRegistry.replaceRegistry(loader.getGemLoader().loadResources());
        toolPartRegistry.replaceRegistry(loader.getToolPartLoader().loadResources());
        toolRegistry.replaceRegistry(loader.getToolLoader().loadResources());
        return this;
    }

    @Override
    public ForgeroRegistry loadResourcesIfEmpty(ForgeroResourceLoader loader) {
        if (isEmpty()) {
            loadResources(loader);
        }
        return this;
    }


    @Override
    public void updateResources(ForgeroResourceLoader loader) {
        materialRegistry.updateRegistry(loader.getMaterialLoader().loadResources());
        schematicRegistry.updateRegistry(loader.getSchematicLoader().loadResources());
        gemRegistry.updateRegistry(loader.getGemLoader().loadResources());
        toolPartRegistry.updateRegistry(loader.getToolPartLoader().loadResources());
        toolRegistry.updateRegistry(loader.getToolLoader().loadResources());
    }
}
