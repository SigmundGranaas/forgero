package com.sigmundgranaas.forgero.minecraft.common.registry.registrar;

import com.sigmundgranaas.forgero.minecraft.common.loot.function.ConditionFunction;
import com.sigmundgranaas.forgero.minecraft.common.loot.function.GemLevelFunction;

import static net.minecraft.registry.Registries.LOOT_FUNCTION_TYPE;

import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class LootFunctionRegistrar implements Registrar {

	public static LootFunctionType GEM_LOOT_FUNCTION_TYPE;
	public static LootFunctionType CONDITION_LOOT_FUNCTION_TYPE;

	public void register() {
		GEM_LOOT_FUNCTION_TYPE = Registry.register(Registry.LOOT_FUNCTION_TYPE, new Identifier("gem_level_function"), new LootFunctionType(new GemLevelFunction.Serializer()));
		CONDITION_LOOT_FUNCTION_TYPE = Registry.register(Registry.LOOT_FUNCTION_TYPE, new Identifier("condition_loot_function"), new LootFunctionType(new ConditionFunction.Serializer()));
	}
}
