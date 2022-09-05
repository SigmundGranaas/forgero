package com.sigmundgranaas.forgero.registry;

import com.sigmundgranaas.forgerocore.ForgeroRegistry;
import com.sigmundgranaas.forgerocore.ForgeroResourceLoader;
import com.sigmundgranaas.forgero.registry.impl.ConcurrentForgeroItemRegistry;

/**
 * Registry for handling all instances of generated Forgero Items
 * <p>
 * All resources in this registry will be registered into Minecraft's systems
 * <p>
 * Thread Safe implementation with ConcurrentForgeroItemRegistry
 */
public interface ForgeroItemRegistry extends ForgeroRegistry {
    ForgeroItemRegistry INSTANCE = ConcurrentForgeroItemRegistry.getInstance();

    GemItemRegistry GEM_ITEM = INSTANCE.getGemItemRegistry();
    SchematicItemRegistry SCHEMATIC_ITEM = INSTANCE.getSchematicItemRegistry();
    ToolPartItemRegistry TOOL_PART_ITEM = INSTANCE.getToolPartItemRegistry();
    ToolItemRegistry TOOL_ITEM = INSTANCE.getToolItemRegistry();

    void register(RegistryHandler handler);

    GemItemRegistry getGemItemRegistry();

    ToolItemRegistry getToolItemRegistry();

    ToolPartItemRegistry getToolPartItemRegistry();

    SchematicItemRegistry getSchematicItemRegistry();

    ForgeroItemRegistry loadResources(ForgeroResourceLoader loader);

    ForgeroItemRegistry loadResourcesIfEmpty(ForgeroResourceLoader loader);

}
