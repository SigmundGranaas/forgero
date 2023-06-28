package com.sigmundgranaas.forgero.fabric.block.assemblystation.state;

import java.util.List;

import com.sigmundgranaas.forgero.minecraft.common.resources.DisassemblyRecipeLoader;

import net.minecraft.item.ItemStack;

public class RecipeHandler implements DisassemblyHandler {
	private final DisassemblyRecipeLoader.DisassemblyRecipe recipe;

	public RecipeHandler(DisassemblyRecipeLoader.DisassemblyRecipe recipe) {
		this.recipe = recipe;
	}

	@Override
	public List<ItemStack> disassemble() {
		return recipe.getResults().stream().map(ItemStack::new).toList();
	}

	@Override
	public DisassemblyHandler insertIntoDisassemblySlot(ItemStack stack) {
		return createHandler(stack);
	}

}
