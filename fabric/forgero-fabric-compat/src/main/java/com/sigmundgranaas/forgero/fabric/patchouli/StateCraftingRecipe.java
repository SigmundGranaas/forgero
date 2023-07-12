package com.sigmundgranaas.forgero.fabric.patchouli;

import static com.sigmundgranaas.forgero.fabric.block.DummyHandler.dummyHandler;

import com.mojang.blaze3d.systems.RenderSystem;
import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.minecraft.common.recipe.customrecipe.RecipeTypes;
import vazkii.patchouli.client.book.ClientBookRegistry;
import vazkii.patchouli.client.book.gui.GuiBook;
import vazkii.patchouli.client.book.page.PageCrafting;

import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;

public class StateCraftingRecipe extends PageCrafting {
	public static Identifier ID = new Identifier(Forgero.NAMESPACE, RecipeTypes.STATE_CRAFTING_RECIPE.getName());

	public StateCraftingRecipe() {
		super();
	}

	public static void register() {
		ClientBookRegistry.INSTANCE.pageTypes.put(ID, StateCraftingRecipe.class);
	}

	@Override
	protected void drawRecipe(MatrixStack ms, Recipe<?> recipe, int recipeX, int recipeY, int mouseX, int mouseY, boolean second) {

		RenderSystem.setShaderTexture(0, book.craftingTexture);
		RenderSystem.enableBlend();
		DrawableHelper.drawTexture(ms, recipeX - 2, recipeY - 2, 0, 0, 100, 62, 128, 256);

		boolean shaped = recipe instanceof ShapedRecipe;
		if (!shaped) {
			int iconX = recipeX + 62;
			int iconY = recipeY + 2;
			DrawableHelper.drawTexture(ms, iconX, iconY, 0, 64, 11, 11, 128, 256);
			if (parent.isMouseInRelativeRange(mouseX, mouseY, iconX, iconY, 11, 11)) {
				parent.setTooltip(Text.translatable("patchouli.gui.lexicon.shapeless"));
			}
		}

		parent.drawCenteredStringNoShadow(ms, getTitle(second).asOrderedText(), GuiBook.PAGE_WIDTH / 2, recipeY - 10, book.headerColor);

		parent.renderItemStack(ms, recipeX + 79, recipeY + 22, mouseX, mouseY, getRecipeOutput(recipe));

		DefaultedList<Ingredient> ingredients = recipe.getIngredients();
		int wrap = 3;
		if (shaped) {
			wrap = ((ShapedRecipe) recipe).getWidth();
		}

		for (int i = 0; i < ingredients.size(); i++) {
			parent.renderIngredient(ms, recipeX + (i % wrap) * 19 + 3, recipeY + (i / wrap) * 19 + 3, mouseX, mouseY, ingredients.get(i));
		}

		parent.renderItemStack(ms, recipeX + 79, recipeY + 41, mouseX, mouseY, recipe.createIcon());

	}

	private CraftingInventory createCraftingInventory(CraftingRecipe recipe) {
		CraftingInventory inventory = new CraftingInventory(dummyHandler, 3, 3);
		if (parent == null) {
			return inventory;
		}
		DefaultedList<Ingredient> ingredients = recipe.getIngredients();

		ItemStack[] heads = ingredients.get(1).getMatchingStacks();
		if (heads.length > 0) {
			inventory.setStack(1, heads[(parent.getTicksInBook() / 20) % heads.length]);
		}

		ItemStack[] handles = ingredients.get(2).getMatchingStacks();
		if (handles.length > 0) {
			inventory.setStack(3, handles[(parent.getTicksInBook() / 20) % heads.length]);
		}

		return inventory;
	}

	@Override
	protected ItemStack getRecipeOutput(Recipe<?> recipe) {
		if (recipe instanceof CraftingRecipe craftingRecipe) {
			return craftingRecipe.craft(createCraftingInventory(craftingRecipe));
		}
		return recipe.getOutput();
	}

	@Override
	protected int getRecipeHeight() {
		return 60;
	}
}
