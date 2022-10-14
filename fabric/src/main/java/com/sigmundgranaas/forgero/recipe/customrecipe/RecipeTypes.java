package com.sigmundgranaas.forgero.recipe.customrecipe;

import java.util.Locale;

public enum RecipeTypes {
    TOOL_PART_SECONDARY_MATERIAL_UPGRADE,
    TOOL_PART_GEM_UPGRADE,
    TOOL_RECIPE,
    GEM_UPGRADE_RECIPE,
    TOOL_PART_RECIPE,
    TOOL_WITH_BINDING_RECIPE,

    MISC_SHAPELESS,

    STATE_CRAFTING_RECIPE,

    
    STATE_UPGRADE_RECIPE,
    TOOLPART_SCHEMATIC_RECIPE;

    String getName() {
        return this.name().toLowerCase(Locale.ROOT);
    }
}
