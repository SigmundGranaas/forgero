package com.sigmundgranaas.forgero.fabric.item;

import com.sigmundgranaas.forgero.core.state.StateProvider;

import com.sigmundgranaas.forgero.core.type.Type;
import com.sigmundgranaas.forgero.core.registry.GenericRegistry;
import com.sigmundgranaas.forgero.core.registry.RankableConverter;

import com.sigmundgranaas.forgero.core.registry.Registerable;

import net.minecraft.item.ItemGroup;
import net.minecraft.registry.Registries;

import static com.sigmundgranaas.forgero.item.RegistryUtils.typeConverter;

public class ItemGroupRegisters implements Registerable<RankableConverter<StateProvider, ItemGroup>> {
	@Override
	public void register(GenericRegistry<RankableConverter<StateProvider, ItemGroup>> registry) {
		registry.register("forgero:tool", typeConverter(Type.TOOL, Registries.ITEM_GROUP.get(net.minecraft.item.ItemGroups.TOOLS), 2));
		registry.register("forgero:weapon", typeConverter(Type.WEAPON, Registries.ITEM_GROUP.get(net.minecraft.item.ItemGroups.COMBAT), 2));
		registry.register("forgero:tool_part", typeConverter(Type.PART, ItemGroups.FORGERO_TOOL_PARTS));
		registry.register("forgero:schematic", typeConverter(Type.SCHEMATIC, ItemGroups.FORGERO_SCHEMATICS));
		registry.register("forgero:trinkets", typeConverter(Type.TRINKET, ItemGroups.FORGERO_GEMS));
	}
}
