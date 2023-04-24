package com.sigmundgranaas.forgero.fabric.patchouli;

import com.mojang.blaze3d.systems.RenderSystem;
import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.core.state.LeveledState;
import com.sigmundgranaas.forgero.minecraft.common.conversion.CachedConverter;
import com.sigmundgranaas.forgero.minecraft.common.recipe.customrecipe.RecipeTypes;
import vazkii.patchouli.client.book.ClientBookRegistry;
import vazkii.patchouli.client.book.gui.GuiBook;
import vazkii.patchouli.client.book.page.abstr.PageDoubleRecipeRegistry;
import vazkii.patchouli.mixin.AccessorSmithingRecipe;

import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.SmithingRecipe;
import net.minecraft.util.Identifier;

public class GemUpgradeRecipePage extends PageDoubleRecipeRegistry<SmithingRecipe> {
	public static Identifier ID = new Identifier(Forgero.NAMESPACE, RecipeTypes.GEM_UPGRADE_RECIPE.getName());

	public GemUpgradeRecipePage() {
		super(RecipeType.SMITHING);
	}

	public static void register() {
		ClientBookRegistry.INSTANCE.pageTypes.put(ID, GemUpgradeRecipePage.class);
	}

	@Override
	protected void drawRecipe(MatrixStack ms, SmithingRecipe recipe, int recipeX, int recipeY, int mouseX, int mouseY, boolean second) {
		RenderSystem.setShaderTexture(0, book.craftingTexture);
		RenderSystem.enableBlend();
		DrawableHelper.drawTexture(ms, recipeX, recipeY, 11, 135, 96, 43, 128, 256);
		parent.drawCenteredStringNoShadow(ms, getTitle(second).asOrderedText(), GuiBook.PAGE_WIDTH / 2, recipeY - 10, book.headerColor);

		parent.renderIngredient(ms, recipeX + 4, recipeY + 4, mouseX, mouseY, ((AccessorSmithingRecipe) recipe).getBase());
		parent.renderIngredient(ms, recipeX + 4, recipeY + 23, mouseX, mouseY, ((AccessorSmithingRecipe) recipe).getAddition());
		parent.renderItemStack(ms, recipeX + 40, recipeY + 13, mouseX, mouseY, recipe.createIcon());
		parent.renderItemStack(ms, recipeX + 76, recipeY + 13, mouseX, mouseY, getRecipeOutput(recipe));
	}

	@Override
	protected ItemStack getRecipeOutput(SmithingRecipe recipe) {
		if (recipe == null) {
			return ItemStack.EMPTY;
		}

		var gemState = CachedConverter.of(recipe.getOutput());
		if (gemState.isPresent() && gemState.get() instanceof LeveledState leveledState) {
			var leveled = leveledState.levelUp();
			return CachedConverter.of(leveled);
		}

		return recipe.getOutput();
	}


	@Override
	protected int getRecipeHeight() {
		return 60;
	}
}
