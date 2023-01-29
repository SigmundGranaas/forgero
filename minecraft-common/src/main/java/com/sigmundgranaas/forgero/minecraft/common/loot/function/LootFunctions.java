package com.sigmundgranaas.forgero.minecraft.common.loot.function;

import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class LootFunctions {
    public static LootFunctionType GEM_LOOT_FUNCTION_TYPE;
    public static LootFunctionType CONDITION_LOOT_FUNCTION_TYPE;

    public static void register() {
        GEM_LOOT_FUNCTION_TYPE = Registry.register(Registry.LOOT_FUNCTION_TYPE, new Identifier("gem_level_function"), new LootFunctionType(new GemLevelFunction.Serializer()));
        CONDITION_LOOT_FUNCTION_TYPE = Registry.register(Registry.LOOT_FUNCTION_TYPE, new Identifier("condition_loot_function"), new LootFunctionType(new ConditionFunction.Serializer()));
    }
}
