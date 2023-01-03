package com.sigmundgranaas.forgero.minecraft.common.recipe.implementation;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.Identifier;

public interface SmithingRecipeGetters {
    Ingredient getBase();

    Ingredient getAddition();

    ItemStack getResult();

    Identifier getId();
}
