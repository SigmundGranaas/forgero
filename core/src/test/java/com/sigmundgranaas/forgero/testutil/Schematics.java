package com.sigmundgranaas.forgero.testutil;

import java.util.List;

import com.sigmundgranaas.forgero.core.state.Ingredient;
import com.sigmundgranaas.forgero.core.state.State;


public class Schematics {
	public static State HANDLE_SCHEMATIC = Ingredient.of("handle-schematic", Types.HANDLE_SCHEMATIC, List.of(Properties.ATTACK_DAMAGE_1));
	public static State REFINED_HANDLE_SCHEMATIC = Ingredient.of("refined_handle-schematic", Types.HANDLE_SCHEMATIC, List.of(Properties.ATTACK_DAMAGE_1));
	public static State SWORD_BLADE_SCHEMATIC = Ingredient.of("sword_blade-schematic", Types.SWORD_BLADE_SCHEMATIC, List.of(Properties.ATTACK_DAMAGE_1));
	public static State PICKAXE_HEAD_SCHEMATIC = Ingredient.of("pickaxe_head-schematic", Types.PICKAXE_SCHEMATIC, List.of(Properties.ATTACK_DAMAGE_1));
	public static State BINDING_SCHEMATIC = Ingredient.of("binding-schematic", Types.BINDING_SCHEMATIC, List.of(Properties.DURABILITY_1000));
	public static State MASTERCRAFTED_SWORD_BLADE_SCHEMATIC = Ingredient.of("mastercrafted_sword_blade-schematic", Types.SWORD_BLADE_SCHEMATIC, List.of(Properties.ATTACK_DAMAGE_1));
	public static State SABER_BLADE_SCHEMATIC = Ingredient.of("saber_blade-schematic", Types.SWORD_BLADE_SCHEMATIC, List.of(Properties.ATTACK_DAMAGE_1));

}
