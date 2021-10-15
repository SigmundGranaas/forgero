package com.sigmundgranaas.forgero.item.forgerotool.recipe;


import com.sigmundgranaas.forgero.item.forgerotool.toolpart.ForgeroToolPartItem;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class HandleRecipe extends SpecialCraftingRecipe {
    private final Ingredient handleIngredient;
    private final ForgeroToolPartItem toolpart;
    private ForgeroRecipeSerializer<?> serializer = null;

    public HandleRecipe(Identifier id, ForgeroToolPartItem toolpart) {
        super(id);
        this.toolpart = toolpart;
        this.handleIngredient = toolpart.getMaterial().getRepairIngredient();
    }

    @Override
    public boolean matches(CraftingInventory inventory, World world) {
        for (int i = 0; i < inventory.size(); ++i) {
            ItemStack currentItems = inventory.getStack(i);
            if (handleIngredient.test(inventory.getStack(i + 2)) && handleIngredient.test(currentItems)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ItemStack craft(CraftingInventory inventory) {
        return new ItemStack(this.toolpart);
    }

    @Override
    public boolean fits(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public boolean isIgnoredInRecipeBook() {
        return false;
    }

    @Override
    public ItemStack getOutput() {
        return new ItemStack(this.toolpart);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return this.serializer;
    }


    public static class Type implements RecipeType<HandleRecipe> {
        public static final Type INSTANCE = new Type();
        // This will be needed in step 4
        public static final String ID = "custom_crafting";

        // Define ExampleRecipe.Type as a singleton by making its constructor private and exposing an instance.
        private Type() {
        }
    }
}