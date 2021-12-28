package com.sigmundgranaas.forgero.recipe;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;

public class ForgeroRecipeTest extends ShapedRecipe {
    public ForgeroRecipeTest(Identifier identifier, String string, int i, int j, DefaultedList<Ingredient> defaultedList, ItemStack itemStack) {
        super(identifier, string, i, j, defaultedList, itemStack);
    }

    public static RecipeSerializer<?> getSerializerInstance() {
        return new Serializer();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.register("forgero_recipe_test", new Serializer());
    }
}
