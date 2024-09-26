package com.sigmundgranaas.forgero.content.compat.patchouli;

import com.mojang.blaze3d.systems.RenderSystem;
import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.core.state.LeveledState;
import com.sigmundgranaas.forgero.recipe.customrecipe.GemUpgradeRecipe;
import com.sigmundgranaas.forgero.recipe.customrecipe.RecipeTypes;

import com.sigmundgranaas.forgero.service.StateService;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.SmithingRecipe;
import net.minecraft.util.Identifier;

import net.minecraft.world.World;

import vazkii.patchouli.client.book.ClientBookRegistry;
import vazkii.patchouli.client.book.gui.GuiBook;
import vazkii.patchouli.client.book.page.abstr.PageDoubleRecipeRegistry;

public class GemUpgradeRecipePage extends PageDoubleRecipeRegistry<SmithingRecipe> {
	public static Identifier ID = new Identifier(Forgero.NAMESPACE, RecipeTypes.GEM_UPGRADE_RECIPE.getName());

	public GemUpgradeRecipePage() {
		super(RecipeType.SMITHING);
	}

	public static void register() {
		ClientBookRegistry.INSTANCE.pageTypes.put(ID, GemUpgradeRecipePage.class);
	}

	@Override
	protected void drawRecipe(DrawContext context, SmithingRecipe recipe, int recipeX, int recipeY, int mouseX, int mouseY, boolean second) {
		RenderSystem.enableBlend();
		context.drawTexture(book.craftingTexture, recipeX, recipeY, 11, 135, 96, 43, 128, 256);
		parent.drawCenteredStringNoShadow(
				context, getTitle(second).asOrderedText(), GuiBook.PAGE_WIDTH / 2, recipeY - 10, book.headerColor);

		parent.renderIngredient(context, recipeX + 4, recipeY + 4, mouseX, mouseY, recipe.getIngredients().get(GemUpgradeRecipe.baseIndex));
		parent.renderIngredient(
				context, recipeX + 4, recipeY + 23, mouseX, mouseY, recipe.getIngredients().get(GemUpgradeRecipe.additionIndex));
		parent.renderItemStack(context, recipeX + 40, recipeY + 13, mouseX, mouseY, recipe.createIcon());
		parent.renderItemStack(
				context, recipeX + 76, recipeY + 13, mouseX, mouseY, getRecipeOutput(MinecraftClient.getInstance().world, recipe));
	}

	@Override
	protected ItemStack getRecipeOutput(World level, SmithingRecipe recipe) {
		if (recipe == null) {
			return ItemStack.EMPTY;
		}

		var gemState = StateService.INSTANCE.convert(recipe.getOutput(level.getRegistryManager()));
		if (gemState.isPresent() && gemState.get() instanceof LeveledState leveledState) {
			var leveled = leveledState.levelUp();

			return StateService.INSTANCE.convert(leveled).orElseGet(() -> recipe.getOutput(level.getRegistryManager()));
		}

		return recipe.getOutput(level.getRegistryManager());
	}

	@Override
	protected int getRecipeHeight() {
		return 60;
	}
}
