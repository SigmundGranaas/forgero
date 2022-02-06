package com.sigmundgranaas.forgero.recipe;

import java.util.Locale;

public enum RecipeTypes {
    HANDLE_RECIPE,
    BINDING_RECIPE,
    PICKAXEHEAD_RECIPE,
    SHOVELHEAD_RECIPE,
    TOOL_PART_SECONDARY_MATERIAL_UPGRADE,
    TOOL_RECIPE,
    GEM_UPGRADE_RECIPE,
    TOOL_WITH_BINDING_RECIPE;

    String getName() {
        return this.name().toLowerCase(Locale.ROOT);
    }
}
