package com.sigmundgranaas.forgero.testutil;

import static com.sigmundgranaas.forgero.testutil.Tools.defaultCompositor;

import java.util.Collections;
import java.util.List;

import com.sigmundgranaas.forgero.core.state.composite.Construct;
import com.sigmundgranaas.forgero.core.state.upgrade.slot.EmptySlot;
import com.sigmundgranaas.forgero.core.type.Type;

public class ToolParts {
	public static Construct HANDLE = Construct.builder()
			.addIngredient(Materials.OAK)
			.addIngredient(Schematics.HANDLE_SCHEMATIC)
			.addUpgrades(EmptySlot.of(List.of(Type.MATERIAL, Type.GEM), Collections.emptySet()))
			.type(Type.HANDLE)
			.compositor(defaultCompositor())
			.build();

	public static Construct REINFORCED_HANDLE = Construct.builder()
			.addIngredient(Materials.OAK)
			.addIngredient(Schematics.REINFORCED_HANDLE_SCHEMATIC)
			.addUpgrades(EmptySlot.of(List.of(Type.MATERIAL, Type.GEM, Type.MATERIAL), Collections.emptySet()))
			.type(Type.HANDLE)
			.compositor(defaultCompositor())
			.build();

	public static Construct PICKAXE_HEAD = Construct.builder()
			.addIngredient(Materials.IRON)
			.addIngredient(Schematics.PICKAXE_HEAD_SCHEMATIC)
			.type(Type.PICKAXE_HEAD)
			.compositor(defaultCompositor())
			.build();

	public static Construct OAK_BINDING = Construct.builder()
			.addIngredient(Materials.OAK)
			.addIngredient(Schematics.BINDING_SCHEMATIC)
			.type(Type.BINDING)
			.compositor(defaultCompositor())
			.build();
}
