package com.sigmundgranaas.forgero.item.forgerotool.recipe;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class ForgeroBaseToolRecipe implements CraftingRecipe {
    private final Ingredient handle;
    private final Ingredient head;
    private final Item itemOutput;
    private final Identifier id;

    public ForgeroBaseToolRecipe(Ingredient handle, Ingredient head, Item itemOutput, Identifier id) {
        this.handle = handle;
        this.head = head;
        this.itemOutput = itemOutput;
        this.id = id;
    }

    public Ingredient getHandle() {
        return handle;
    }

    public Item getItemOutput() {
        return itemOutput;
    }

    public Ingredient getHead() {
        return head;
    }

    @Override
    public boolean matches(CraftingInventory inventory, World world) {
        for (int i = 0; i < inventory.size(); ++i) {
            ItemStack currentItems = inventory.getStack(i);
            if (head.test(inventory.getStack(i + 3)) && handle.test(currentItems)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ItemStack craft(CraftingInventory inventory) {
        ItemStack head_item;
        ItemStack handle_item;
        for (int i = 0; i < inventory.size(); ++i) {
            ItemStack currentItems = inventory.getStack(i);
            if (head.test(inventory.getStack(i + 3)) && handle.test(currentItems)) {
                head_item = inventory.getStack(i + 3);
                handle_item = inventory.getStack(i);
            }
        }
        ItemStack output = new ItemStack(this.itemOutput);
        return output;
    }

    @Override
    public boolean fits(int width, int height) {
        return width * height >= 4;
    }

    @Override
    public boolean isIgnoredInRecipeBook() {
        return false;
    }

    @Override
    public Identifier getId() {
        return this.id;
    }

    @Override
    public ItemStack getOutput() {
        return new ItemStack(this.itemOutput);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ForgeroBaseToolRecipeSerializer.INSTANCE;
    }
}