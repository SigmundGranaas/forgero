package com.sigmundgranaas.forgero.registry;

import com.sigmundgranaas.forgero.core.ForgeroRegistry;
import com.sigmundgranaas.forgero.core.ForgeroResourceInitializer;
import com.sigmundgranaas.forgero.registry.impl.ConcurrentForgeroFabricRegistry;
import com.sigmundgranaas.forgero.registry.impl.GemItemRegistry;
import com.sigmundgranaas.forgero.registry.impl.SchematicItemRegistry;

public interface ForgeroFabricRegistry extends ForgeroRegistry {
    ForgeroFabricRegistry INSTANCE = ConcurrentForgeroFabricRegistry.getInstance();

    GemItemRegistry GEM_ITEM = INSTANCE.getGemItemRegistry();
    SchematicItemRegistry SCHEMATIC_ITEM = INSTANCE.getSchematicItemRegistry();
    ToolPartItemRegistry TOOL_PART_ITEM = INSTANCE.getToolPartItemRegistry();
    ToolItemRegistry TOOL_ITEM = INSTANCE.getToolItemRegistry();

    GemItemRegistry getGemItemRegistry();

    ToolItemRegistry getToolItemRegistry();

    ToolPartItemRegistry getToolPartItemRegistry();

    SchematicItemRegistry getSchematicItemRegistry();

    ForgeroFabricRegistry loadResources(ForgeroResourceInitializer initializer);

    ForgeroFabricRegistry loadResourcesIfEmpty(ForgeroResourceInitializer initializer);

}
