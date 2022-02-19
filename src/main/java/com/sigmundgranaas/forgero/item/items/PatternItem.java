package com.sigmundgranaas.forgero.item.items;

import com.sigmundgranaas.forgero.recipe.customrecipe.RecipeTypes;
import net.minecraft.item.Item;

public class PatternItem extends Item {
    private final RecipeTypes type;

    public PatternItem(Settings settings, RecipeTypes type) {
        super(settings);
        this.type = type;
    }

    public RecipeTypes getType() {
        return type;
    }

}
