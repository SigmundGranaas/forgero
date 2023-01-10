package com.sigmundgranaas.forgero.minecraft.common.recipe.customrecipe;

import java.util.Locale;

public enum RecipeTypes {

    MISC_SHAPELESS,

    REPAIR_KIT_RECIPE,
    GEM_UPGRADE_RECIPE,
    STATE_CRAFTING_RECIPE,
    STATE_UPGRADE_RECIPE,
    SCHEMATIC_PART_CRAFTING,
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
