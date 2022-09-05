package com.sigmundgranaas.forgerocore;

import com.sigmundgranaas.forgerocore.registry.*;
import com.sigmundgranaas.forgerocore.registry.impl.ConcurrentForgeroRegistry;

/**
 * The root registry for Forgero. All available resource will be stored within the child registries.
 * Defaults to a ConcurrentRegistry, which makes it Thread safe.
 * use loadResourcesIfEmpty
 */
public interface ForgeroRegistry {
    ForgeroRegistry INSTANCE = ConcurrentForgeroRegistry.getInstance();

    MaterialRegistry MATERIAL = INSTANCE.getMaterialRegistry();
    GemRegistry GEM = INSTANCE.getGemRegistry();
    SchematicRegistry SCHEMATIC = INSTANCE.getSchematicRegistry();
    ToolPartRegistry TOOL_PART = INSTANCE.getToolPartRegistry();
    ToolRegistry TOOL = INSTANCE.getToolRegistry();

    ForgeroRegistry loadResources(ForgeroResourceLoader loader);

    ForgeroRegistry loadResourcesIfEmpty(ForgeroResourceLoader loader);

    void updateResources(ForgeroResourceLoader loader);

    void clear();

    boolean isEmpty();

    MaterialRegistry getMaterialRegistry();

    GemRegistry getGemRegistry();

    ToolRegistry getToolRegistry();

    ToolPartRegistry getToolPartRegistry();

    SchematicRegistry getSchematicRegistry();

}
