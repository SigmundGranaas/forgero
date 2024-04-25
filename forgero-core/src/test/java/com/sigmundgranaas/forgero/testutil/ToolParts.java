package com.sigmundgranaas.forgero.testutil;

import static com.sigmundgranaas.forgero.testutil.Materials.IRON;
import static com.sigmundgranaas.forgero.testutil.Materials.OAK;

import java.util.Collections;
import java.util.List;

import com.sigmundgranaas.forgero.core.state.composite.Construct;
import com.sigmundgranaas.forgero.core.state.upgrade.slot.EmptySlot;
import com.sigmundgranaas.forgero.core.type.Type;

public class ToolParts {
	public static Construct HANDLE = Construct.builder()
			.addIngredient(OAK)
			.addIngredient(Schematics.HANDLE_SCHEMATIC)
			.addUpgrades(EmptySlot.of(List.of(Types.MATERIAL, Types.GEM), Collections.emptySet()))
			.type(Type.HANDLE)
			.build();

	public static Construct REFINED_HANDLE = Construct.builder()
			.addIngredient(OAK)
			.addIngredient(Schematics.REFINED_HANDLE_SCHEMATIC)
			.addUpgrades(EmptySlot.of(List.of(Types.MATERIAL, Types.GEM, Types.MATERIAL), Collections.emptySet()))
			.type(Type.HANDLE)
			.build();

	public static Construct PICKAXE_HEAD = Construct.builder()
			.addIngredient(IRON)
			.addIngredient(Schematics.PICKAXE_HEAD_SCHEMATIC)
			.type(Type.PICKAXE_HEAD)
			.build();

	public static Construct SWORD_BLADE = Construct.builder()
			.addIngredient(IRON)
			.addIngredient(Schematics.SWORD_BLADE_SCHEMATIC)
			.type(Type.SWORD_BLADE)
			.build();


	public static Construct MASTERCRAFTED_SWORD_BLADE = Construct.builder()
			.addIngredient(IRON)
			.addIngredient(Schematics.MASTERCRAFTED_SWORD_BLADE_SCHEMATIC)
			.type(Type.SWORD_BLADE)
			.build();

	public static Construct SABER_BLADE = Construct.builder()
			.addIngredient(IRON)
			.addIngredient(Schematics.SABER_BLADE_SCHEMATIC)
			.type(Type.SWORD_BLADE)
			.build();

	public static Construct OAK_BINDING = Construct.builder()
			.addIngredient(OAK)
			.addIngredient(Schematics.BINDING_SCHEMATIC)
			.type(Type.BINDING)
			.build();
}
