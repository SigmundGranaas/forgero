package com.sigmundgranaas.forgero.testutil;

import static com.sigmundgranaas.forgero.testutil.Properties.ATTACK_DAMAGE_1;

import java.util.List;

import com.sigmundgranaas.forgero.core.state.Ingredient;


public class Schematics {
	public static Ingredient HANDLE_SCHEMATIC = Ingredient.of("handle-schematic", Types.HANDLE_SCHEMATIC, List.of(ATTACK_DAMAGE_1));
	public static Ingredient REINFORCED_HANDLE_SCHEMATIC = Ingredient.of("reinforced_handle-schematic", Types.HANDLE_SCHEMATIC, List.of(ATTACK_DAMAGE_1));
	public static Ingredient PICKAXE_HEAD_SCHEMATIC = Ingredient.of("pickaxe_head-schematic", Types.PICKAXE_SCHEMATIC, List.of(ATTACK_DAMAGE_1));
	public static Ingredient BINDING_SCHEMATIC = Ingredient.of("binding-schematic", Types.BINDING_SCHEMATIC, List.of(Properties.DURABILITY_1000));
}
