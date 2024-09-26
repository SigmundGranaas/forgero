package com.sigmundgranaas.forgero.content.compat.patchouli;

import com.mojang.blaze3d.systems.RenderSystem;
import com.sigmundgranaas.forgero.core.Forgero;

import com.sigmundgranaas.forgero.recipe.customrecipe.RecipeTypes;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import net.minecraft.util.collection.DefaultedList;

import net.minecraft.world.World;

import vazkii.patchouli.client.book.ClientBookRegistry;
import vazkii.patchouli.client.book.gui.GuiBook;
import vazkii.patchouli.client.book.page.PageCrafting;

import static com.sigmundgranaas.forgero.block.assemblystation.AssemblyStationScreenHandler.dummyHandler;

public class StateCraftingRecipePage extends PageCrafting {
	public static Identifier ID = new Identifier(Forgero.NAMESPACE, RecipeTypes.STATE_CRAFTING_RECIPE.getName());

	public StateCraftingRecipePage() {
		super();
	}

	public static void register() {
		ClientBookRegistry.INSTANCE.pageTypes.put(ID, StateCraftingRecipePage.class);
	}

	@Override
	protected void drawRecipe(DrawContext context, Recipe<?> recipe, int recipeX, int recipeY, int mouseX, int mouseY, boolean second) {
		RenderSystem.enableBlend();
		context.drawTexture(book.craftingTexture, recipeX - 2, recipeY - 2, 0, 0, 100, 62, 128, 256);

		boolean shaped = recipe instanceof ShapedRecipe;
		if (!shaped) {
			int iconX = recipeX + 62;
			int iconY = recipeY + 2;
			context.drawTexture(book.craftingTexture, iconX, iconY, 0, 64, 11, 11, 128, 256);
			if (parent.isMouseInRelativeRange(mouseX, mouseY, iconX, iconY, 11, 11)) {
				parent.setTooltip(Text.translatable("patchouli.gui.lexicon.shapeless"));
			}
		}

		parent.drawCenteredStringNoShadow(
				context, getTitle(second).asOrderedText(), GuiBook.PAGE_WIDTH / 2, recipeY - 10, book.headerColor);

		parent.renderItemStack(
				context, recipeX + 79, recipeY + 22, mouseX, mouseY, getRecipeOutput(MinecraftClient.getInstance().world, recipe));

		DefaultedList<Ingredient> ingredients = recipe.getIngredients();
		int wrap = 3;
		if (shaped) {
			wrap = ((ShapedRecipe) recipe).getWidth();
		}

		for (int i = 0; i < ingredients.size(); i++) {
			parent.renderIngredient(
					context, recipeX + (i % wrap) * 19 + 3, recipeY + (i / wrap) * 19 + 3, mouseX, mouseY, ingredients.get(i));
		}

		parent.renderItemStack(context, recipeX + 79, recipeY + 41, mouseX, mouseY, recipe.createIcon());
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
	protected ItemStack getRecipeOutput(World level, Recipe<?> recipe) {
		if (recipe instanceof CraftingRecipe craftingRecipe) {
			return craftingRecipe.craft(createCraftingInventory(craftingRecipe), level.getRegistryManager());
		}
		return recipe.getOutput(level.getRegistryManager());
	}

	@Override
	protected int getRecipeHeight() {
		return 60;
	}
}
