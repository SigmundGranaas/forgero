package com.sigmundgranaas.forgero.recipe;

import com.sigmundgranaas.forgero.item.tool.ForgeroPickaxeItem;
import com.sigmundgranaas.forgero.item.tool.ForgeroShovelItem;
import com.sigmundgranaas.forgero.item.toolpart.ForgeroToolPartItemImpl;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class ForgeroToolWithBindingRecipe implements CraftingRecipe {
    private final Ingredient handle;
    private final Ingredient head;
    private final Ingredient binding;
    private final Item itemOutput;
    private final Identifier id;

    public ForgeroToolWithBindingRecipe(Ingredient head, Ingredient handle, Ingredient binding, Item itemOutput, Identifier id) {
        this.handle = handle;
        this.head = head;
        this.binding = binding;
        this.itemOutput = itemOutput;
        this.id = id;
    }

    public Ingredient getBinding() {
        return binding;
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
            if (head.test(currentItems) && binding.test(inventory.getStack(i + 3)) && handle.test(inventory.getStack(i + 6))) {
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
        String bindingType = ((ForgeroToolPartItemImpl) binding.getMatchingStacks()[0].getItem()).getToolPartTypeAndMaterialLowerCase();
        if (this.itemOutput instanceof ForgeroShovelItem) {
            bindingType += "_toolpart_shovel";
        } else if (this.itemOutput instanceof ForgeroPickaxeItem) {
            bindingType += "_toolpart_pickaxe";
        }
        output.getOrCreateNbt().putString("binding", bindingType);
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