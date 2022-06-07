package com.sigmundgranaas.forgero.registry.impl;

import com.sigmundgranaas.forgero.core.ForgeroRegistry;
import com.sigmundgranaas.forgero.core.ForgeroResourceLoader;
import com.sigmundgranaas.forgero.core.registry.*;
import com.sigmundgranaas.forgero.item.ItemFactory;
import com.sigmundgranaas.forgero.item.ToolPartItem;
import com.sigmundgranaas.forgero.registry.*;
import org.jetbrains.annotations.NotNull;

import javax.annotation.concurrent.ThreadSafe;
import java.util.HashMap;

/**
 *
 */
@ThreadSafe
public class ConcurrentForgeroItemRegistry implements ForgeroItemRegistry {
    private volatile static ConcurrentForgeroItemRegistry INSTANCE;

    private final ToolItemRegistry toolItemRegistry = new ConcurrentToolItemRegistry(new HashMap<>());
    private final ToolPartItemRegistry toolPartItemRegistry = new ConcurrentToolPartItemRegistry(new HashMap<>());
    private final GemItemRegistry gemItemRegistry = new ConcurrentGemItemRegistry(new HashMap<>());
    private final SchematicItemRegistry schematicItemRegistry = new ConcurrentSchematicItemRegistry(new HashMap<>());

    ForgeroRegistry forgeroRegistry;

    private ConcurrentForgeroItemRegistry() {
        this.forgeroRegistry = ForgeroRegistry.INSTANCE;
    }

    public static ConcurrentForgeroItemRegistry getInstance() {
        if (INSTANCE == null) {
            synchronized (ConcurrentForgeroItemRegistry.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ConcurrentForgeroItemRegistry();
                }
            }
        }
        return INSTANCE;
    }

    @Override
    public ForgeroItemRegistry loadResources(ForgeroResourceLoader loader) {
        forgeroRegistry.loadResources(loader);

        schematicItemRegistry.replaceRegistry(SCHEMATIC.stream()
                .map(ItemFactory.INSTANCE::createSchematic)
                .toList());
        gemItemRegistry.replaceRegistry(GEM.list().stream()
                .map(ItemFactory.INSTANCE::createGem)
                .toList());
        toolItemRegistry.replaceRegistry(TOOL.list().stream()
                .map(ItemFactory.INSTANCE::createTool)
                .toList());
        toolPartItemRegistry.replaceRegistry(TOOL_PART.list().stream()
                .filter(toolPart -> toolPart.getSchematic().getResourceName().contains("default"))
                .map(ItemFactory.INSTANCE::createToolPart)
                .map(ToolPartItem.class::cast)
                .toList());
        return this;
    }

    @Override
    @NotNull
    public ForgeroItemRegistry loadResourcesIfEmpty(ForgeroResourceLoader loader) {
        if (isEmpty()) {
            loadResources(loader);
        }
        return this;
    }

    @Override
    public void updateResources(ForgeroResourceLoader loader) {
        forgeroRegistry.updateResources(loader);
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
        return forgeroRegistry.isEmpty()
                || toolItemRegistry.isEmpty()
                || toolPartItemRegistry.isEmpty()
                || schematicItemRegistry.isEmpty()
                || gemItemRegistry.isEmpty();
    }


    @Override
    public void register(RegistryHandler handler) {
        toolItemRegistry.register(handler);
        toolPartItemRegistry.register(handler);
        gemItemRegistry.register(handler);
        schematicItemRegistry.register(handler);
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
