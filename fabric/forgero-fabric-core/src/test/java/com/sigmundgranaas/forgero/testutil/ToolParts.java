package com.sigmundgranaas.forgero.testutil;

import java.util.Collections;
import java.util.List;

import com.sigmundgranaas.forgero.core.state.composite.Construct;
import com.sigmundgranaas.forgero.core.state.upgrade.slot.EmptySlot;

public class ToolParts {
	public static Construct HANDLE = Construct.builder()
			.addIngredient(Materials.OAK)
			.addIngredient(Schematics.HANDLE_SCHEMATIC)
			.addUpgrades(EmptySlot.of(List.of(Types.MATERIAL, Types.GEM), Collections.emptySet()))
			.type(Types.HANDLE)
			.build();

	public static Construct REINFORCED_HANDLE = Construct.builder()
			.addIngredient(Materials.OAK)
			.addIngredient(Schematics.REINFORCED_HANDLE_SCHEMATIC)
			.addUpgrades(EmptySlot.of(List.of(Types.MATERIAL, Types.GEM, Types.MATERIAL), Collections.emptySet()))
			.type(Types.HANDLE)
			.build();

	public static Construct PICKAXE_HEAD = Construct.builder()
			.addIngredient(Materials.IRON)
			.addIngredient(Schematics.PICKAXE_HEAD_SCHEMATIC)
			.type(Types.TOOL_PART_HEAD)
			.build();

	public static Construct OAK_BINDING = Construct.builder()
			.addIngredient(Materials.OAK)
			.addIngredient(Schematics.BINDING_SCHEMATIC)
			.type(Types.BINDING)
			.build();
}
