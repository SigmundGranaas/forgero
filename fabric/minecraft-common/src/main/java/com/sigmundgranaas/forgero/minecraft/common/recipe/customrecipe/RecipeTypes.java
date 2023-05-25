package com.sigmundgranaas.forgero.minecraft.common.recipe.customrecipe;

import java.util.Locale;

public enum RecipeTypes {

	MISC_SHAPELESS,
	BASIC_AXE_HEAD,
	BASIC_SWORD_BLADE,
	BASIC_SHORT_SWORD_BLADE,

	BASIC_SWORD_GUARD,
	BASIC_PICKAXE_HEAD,
	BASIC_HOE_HEAD,
	BASIC_SHOVEL_HEAD,
	BASIC_HANDLE,

	ANY_PART_TO_STONE,

	REPAIR_KIT_RECIPE,
	GEM_UPGRADE_RECIPE,
	STATE_CRAFTING_RECIPE,
	STATE_UPGRADE_RECIPE,
	STATE_UPGRADE_CRAFTING_TABLE_RECIPE,
	SCHEMATIC_PART_CRAFTING,
	PART_SMELTING_RECIPE,
	PART_BLASTING_RECIPE,
	TOOLPART_SCHEMATIC_RECIPE;

	public static RecipeTypes of(String type) {
		try {
			return RecipeTypes.valueOf(type.toUpperCase());
		} catch (Exception e) {
			return RecipeTypes.MISC_SHAPELESS;
		}
	}

	public String getName() {
		return this.name().toLowerCase(Locale.ROOT);
	}
}
