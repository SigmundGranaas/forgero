package com.sigmundgranaas.forgero.registry.impl;

import com.sigmundgranaas.forgero.core.ForgeroRegistry;
import com.sigmundgranaas.forgero.core.ForgeroResourceInitializer;
import com.sigmundgranaas.forgero.core.registry.*;
import com.sigmundgranaas.forgero.item.ForgeroToolItem;
import com.sigmundgranaas.forgero.item.ItemFactory;
import com.sigmundgranaas.forgero.item.ToolPartItem;
import com.sigmundgranaas.forgero.registry.ForgeroFabricRegistry;
import com.sigmundgranaas.forgero.registry.ToolItemRegistry;
import com.sigmundgranaas.forgero.registry.ToolPartItemRegistry;

import java.util.HashMap;

public class ConcurrentForgeroFabricRegistry implements ForgeroFabricRegistry {
    private volatile static ConcurrentForgeroFabricRegistry INSTANCE;

    private final ToolItemRegistry toolItemRegistry = new ConcurrentToolItemRegistry(new HashMap<>());
    private final ToolPartItemRegistry toolPartItemRegistry = new ConcurrentToolPartItemRegistry(new HashMap<>());
    private final GemItemRegistry gemItemRegistry = new ConcurrentGemItemRegistry(new HashMap<>());
    private final SchematicItemRegistry schematicItemRegistry = new ConcurrentSchematicItemRegistry(new HashMap<>());

    ForgeroRegistry forgeroRegistry;

    private ConcurrentForgeroFabricRegistry() {
        this.forgeroRegistry = ForgeroRegistry.INSTANCE;
    }

    public static ConcurrentForgeroFabricRegistry getInstance() {
        if (INSTANCE == null) {
            synchronized (ConcurrentForgeroFabricRegistry.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ConcurrentForgeroFabricRegistry();
                }
            }
        }
        return INSTANCE;
    }

    @Override
    public ForgeroFabricRegistry loadResources(ForgeroResourceInitializer initializer) {
        forgeroRegistry.loadResources(initializer);
        schematicItemRegistry.updateRegistry(SCHEMATIC.list().stream()
                .map(ItemFactory.INSTANCE::createSchematic)
                .toList());
        gemItemRegistry.updateRegistry(GEM.list().stream()
                .map(ItemFactory.INSTANCE::createGem)
                .toList());
        toolItemRegistry.updateRegistry(TOOL.list().stream()
                .map(ItemFactory.INSTANCE::createTool)
                .map(ForgeroToolItem.class::cast)
                .toList());
        toolPartItemRegistry.updateRegistry(TOOL_PART.list().stream()
                .map(ItemFactory.INSTANCE::createToolPart)
                .map(ToolPartItem.class::cast)
                .toList());
        return this;
    }

    @Override
    public ForgeroFabricRegistry loadResourcesIfEmpty(ForgeroResourceInitializer initializer) {
        if (isEmpty()) {
            loadResources(initializer);
        }
        return this;
    }

    @Override
    public void updateResources() {

    }

    @Override
    public void clear() {
        forgeroRegistry.clear();
        gemItemRegistry.clear();
        toolItemRegistry.clear();
        toolPartItemRegistry.clear();
        schematicItemRegistry.clear();
    }

    @Override
    public boolean isEmpty() {
        return false;
    }


    @Override
    public GemItemRegistry getGemItemRegistry() {
        return gemItemRegistry;
    }

    @Override
    public ToolItemRegistry getToolItemRegistry() {
        return toolItemRegistry;
    }

    @Override
    public ToolPartItemRegistry getToolPartItemRegistry() {
        return toolPartItemRegistry;
    }

    @Override
    public SchematicItemRegistry getSchematicItemRegistry() {
        return schematicItemRegistry;
    }

    @Override
    public MaterialRegistry getMaterialRegistry() {
        return forgeroRegistry.getMaterialRegistry();
    }

    @Override
    public GemRegistry getGemRegistry() {
        return forgeroRegistry.getGemRegistry();
    }

    @Override
    public ToolRegistry getToolRegistry() {
        return forgeroRegistry.getToolRegistry();
    }

    @Override
    public ToolPartRegistry getToolPartRegistry() {
        return forgeroRegistry.getToolPartRegistry();
    }

    @Override
    public SchematicRegistry getSchematicRegistry() {
        return forgeroRegistry.getSchematicRegistry();
    }
}
