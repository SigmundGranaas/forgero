package com.sigmundgranaas.forgero.fabric.item;

import com.sigmundgranaas.forgero.core.state.StateProvider;

import com.sigmundgranaas.forgero.core.type.Type;
import com.sigmundgranaas.forgero.minecraft.common.item.GenericRegistry;
import com.sigmundgranaas.forgero.minecraft.common.item.RankableConverter;

import com.sigmundgranaas.forgero.minecraft.common.item.Registerable;

import net.minecraft.item.ItemGroup;

import static com.sigmundgranaas.forgero.minecraft.common.item.RegistryUtils.typeConverter;

public class ItemGroupRegisters implements Registerable<RankableConverter<StateProvider, ItemGroup>> {
	@Override
	public void register(GenericRegistry<RankableConverter<StateProvider, ItemGroup>> registry) {
		registry.register("forgero:tool", typeConverter(Type.TOOL, ItemGroup.TOOLS, 2));
		registry.register("forgero:weapon", typeConverter(Type.WEAPON, ItemGroup.COMBAT, 2));
		registry.register("forgero:tool_part", typeConverter(Type.PART, ItemGroups.FORGERO_TOOL_PARTS));
		registry.register("forgero:schematic", typeConverter(Type.SCHEMATIC, ItemGroups.FORGERO_SCHEMATICS));
		registry.register("forgero:trinkets", typeConverter(Type.TRINKET, ItemGroups.FORGERO_GEMS));
	}
}
