package com.sigmundgranaas.forgero.minecraft.common.registry.registrar;

import com.sigmundgranaas.forgero.minecraft.common.loot.function.GemLevelFunction;

import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class LootFunctionRegistrar implements Registrar {

	public static LootFunctionType GEM_LOOT_FUNCTION_TYPE;

	public void register() {
		GEM_LOOT_FUNCTION_TYPE = Registry.register(Registries.LOOT_FUNCTION_TYPE, new Identifier("gem_level_function"), new LootFunctionType(new GemLevelFunction.Serializer()));
	}
}
