package com.sigmundgranaas.forgero.fabric.patchouli;

import com.mojang.blaze3d.systems.RenderSystem;
import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.minecraft.common.recipe.customrecipe.RecipeTypes;

import com.sigmundgranaas.forgero.minecraft.common.recipe.customrecipe.StateUpgradeRecipe;

import com.terraformersmc.modmenu.util.DrawingUtil;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.SmithingRecipe;
import net.minecraft.util.Identifier;

import net.minecraft.world.World;

import vazkii.patchouli.client.book.ClientBookRegistry;
import vazkii.patchouli.client.book.gui.GuiBook;
import vazkii.patchouli.client.book.page.abstr.PageDoubleRecipeRegistry;

import static com.sigmundgranaas.forgero.minecraft.common.block.assemblystation.AssemblyStationScreenHandler.dummyHandler;

public class StateUpgradeRecipePage extends PageDoubleRecipeRegistry<SmithingRecipe> {
	public static Identifier ID = new Identifier(Forgero.NAMESPACE, RecipeTypes.STATE_UPGRADE_RECIPE.getName());

	public StateUpgradeRecipePage() {
		super(RecipeType.SMITHING);
	}

	public static void register() {
		ClientBookRegistry.INSTANCE.pageTypes.put(ID, StateUpgradeRecipePage.class);
	}

	@Override
	protected void drawRecipe(DrawContext context, SmithingRecipe recipe, int recipeX, int recipeY, int mouseX, int mouseY, boolean second) {
		RenderSystem.enableBlend();

		context.drawTexture(book.craftingTexture, recipeX, recipeY, 11, 135, 96, 43, 128, 256);
		parent.drawCenteredStringNoShadow(context, getTitle(second).asOrderedText(), GuiBook.PAGE_WIDTH / 2, recipeY - 10, book.headerColor);
		CraftingInventory inventory = createCraftingInventory(recipe);
		parent.renderIngredient(context, recipeX + 4, recipeY + 4, mouseX, mouseY, recipe.getIngredients().get(StateUpgradeRecipe.baseIndex));
		parent.renderIngredient(context, recipeX + 4, recipeY + 23, mouseX, mouseY, recipe.getIngredients().get(StateUpgradeRecipe.additionIndex));
		parent.renderItemStack(context, recipeX + 40, recipeY + 13, mouseX, mouseY, recipe.createIcon());
		parent.renderItemStack(context, recipeX + 76, recipeY + 13, mouseX, mouseY, recipe.craft(inventory, null));
	}

	@Override
	protected ItemStack getRecipeOutput(World level, SmithingRecipe recipe) {
		return recipe.getResult(level.getRegistryManager());
	}

	private CraftingInventory createCraftingInventory(SmithingRecipe recipe) {
		CraftingInventory inventory = new CraftingInventory(dummyHandler, 2, 1);
		if (parent == null) {
			return inventory;
		}

		if(recipe instanceof StateUpgradeRecipe legacySmithingRecipe){
			ItemStack[] original = legacySmithingRecipe.base.getMatchingStacks();
			if (original.length > 0) {
				inventory.setStack(0, original[(parent.getTicksInBook() / 20) % original.length]);
			}

			ItemStack[] upgrade = legacySmithingRecipe.addition.getMatchingStacks();
			if (upgrade.length > 0) {
				inventory.setStack(1, upgrade[(parent.getTicksInBook() / 20) % upgrade.length]);
			}
		}
		return inventory;
	}

	@Override
	protected int getRecipeHeight() {
		return 60;
	}
}
