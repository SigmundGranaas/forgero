package com.sigmundgranaas.forgero.core;

import com.sigmundgranaas.forgero.core.registry.*;
import com.sigmundgranaas.forgero.core.registry.impl.ConcurrentForgeroRegistry;

/**
 * The root registry for Forgero. All available resource will be stored within the child registries.
 */
public interface ForgeroRegistry {
    ForgeroRegistry INSTANCE = ConcurrentForgeroRegistry.getInstance();

    MaterialRegistry MATERIAL = INSTANCE.getMaterialRegistry();
    GemRegistry GEM = INSTANCE.getGemRegistry();
    SchematicRegistry SCHEMATIC = INSTANCE.getSchematicRegistry();
    ToolPartRegistry TOOL_PART = INSTANCE.getToolPartRegistry();
    ToolRegistry TOOL = INSTANCE.getToolRegistry();

    void loadResources(ForgeroResourceInitializer initializer);

    void updateResources();

    void clear();

    MaterialRegistry getMaterialRegistry();

    GemRegistry getGemRegistry();

    ToolRegistry getToolRegistry();

    ToolPartRegistry getToolPartRegistry();

    SchematicRegistry getSchematicRegistry();

}
