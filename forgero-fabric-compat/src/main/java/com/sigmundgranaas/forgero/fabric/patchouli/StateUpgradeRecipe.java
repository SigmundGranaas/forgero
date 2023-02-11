package com.sigmundgranaas.forgero.fabric.patchouli;

import com.mojang.blaze3d.systems.RenderSystem;
import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.minecraft.common.recipe.customrecipe.RecipeTypes;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.SmithingRecipe;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import vazkii.patchouli.client.book.ClientBookRegistry;
import vazkii.patchouli.client.book.gui.GuiBook;
import vazkii.patchouli.client.book.page.abstr.PageDoubleRecipeRegistry;
import vazkii.patchouli.mixin.AccessorSmithingRecipe;

import static com.sigmundgranaas.forgero.minecraft.common.block.assemblystation.AssemblyStationScreenHandler.dummyHandler;

public class StateUpgradeRecipe extends PageDoubleRecipeRegistry<SmithingRecipe> {
    public static Identifier ID = new Identifier(Forgero.NAMESPACE, RecipeTypes.STATE_UPGRADE_RECIPE.getName());

    public StateUpgradeRecipe() {
        super(RecipeType.SMITHING);
    }

    public static void register() {
        ClientBookRegistry.INSTANCE.pageTypes.put(ID, StateUpgradeRecipe.class);
    }

    @Override
    protected void drawRecipe(MatrixStack ms, SmithingRecipe recipe, int recipeX, int recipeY, int mouseX, int mouseY, boolean second) {
        RenderSystem.setShaderTexture(0, book.craftingTexture);
        RenderSystem.enableBlend();
        DrawableHelper.drawTexture(ms, recipeX, recipeY, 11, 135, 96, 43, 128, 256);
        parent.drawCenteredStringNoShadow(ms, getTitle(second).asOrderedText(), GuiBook.PAGE_WIDTH / 2, recipeY - 10, book.headerColor);
        CraftingInventory inventory = createCraftingInventory(recipe);
        parent.renderIngredient(ms, recipeX + 4, recipeY + 4, mouseX, mouseY, ((AccessorSmithingRecipe) recipe).getBase());
        parent.renderIngredient(ms, recipeX + 4, recipeY + 23, mouseX, mouseY, ((AccessorSmithingRecipe) recipe).getAddition());
        parent.renderItemStack(ms, recipeX + 40, recipeY + 13, mouseX, mouseY, recipe.createIcon());
        parent.renderItemStack(ms, recipeX + 76, recipeY + 13, mouseX, mouseY, recipe.craft(inventory));
    }

    @Override
    protected ItemStack getRecipeOutput(SmithingRecipe recipe) {
        return recipe.getOutput();
    }

    private CraftingInventory createCraftingInventory(SmithingRecipe recipe) {
        CraftingInventory inventory = new CraftingInventory(dummyHandler, 2, 1);
        if (parent == null) {
            return inventory;
        }
        DefaultedList<Ingredient> ingredients = recipe.getIngredients();

        ItemStack[] original = ((SmithingRecipeGetters) recipe).getBase().getMatchingStacks();
        if (original.length > 0) {
            inventory.setStack(0, original[(parent.getTicksInBook() / 20) % original.length]);
        }

        ItemStack[] upgrade = ((SmithingRecipeGetters) recipe).getAddition().getMatchingStacks();
        if (upgrade.length > 0) {
            inventory.setStack(1, upgrade[(parent.getTicksInBook() / 20) % upgrade.length]);
        }

        return inventory;
    }

    @Override
    protected int getRecipeHeight() {
        return 60;
    }
}