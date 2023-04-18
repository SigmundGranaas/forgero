package com.sigmundgranaas.forgero.minecraft.common.block.assemblystation.state;

import java.util.List;
import java.util.Optional;

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
	public Optional<DisassemblyHandler> insertIntoDisassemblySlot(ItemStack stack) {
		return Optional.empty();
	}

	@Override
	public void clearResults() {

	}

	@Override
	public DisassemblyHandler removeItemFromDeconstructionSlot() {
		return new EmptyHandler();
	}


	@Override
	public DisassemblyHandler removeItemFromResultSlot() {
		return new EmptyHandler();
	}


	@Override
	public void clear() {

	}
}
