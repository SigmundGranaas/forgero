package com.sigmundgranaas.forgero.minecraft.common.block.assemblystation.state;

import java.util.List;
import java.util.Optional;

import com.sigmundgranaas.forgero.core.state.Composite;
import com.sigmundgranaas.forgero.minecraft.common.conversion.StateConverter;
import com.sigmundgranaas.forgero.minecraft.common.resources.DisassemblyRecipeLoader;

import net.minecraft.item.ItemStack;

public interface DisassemblyHandler {
	static Optional<DisassemblyHandler> createHandler(ItemStack stack) {
		boolean noDamage = stack.getDamage() == 0;

		if (noDamage && StateConverter.of(stack).isPresent() && StateConverter.of(stack).get() instanceof Composite composite) {
			return Optional.of(new CompositeHandler(composite));
		} else if (noDamage && DisassemblyRecipeLoader.getEntries().stream()
				.anyMatch(entry -> entry.getInput().test(stack))) {
			return Optional.of(new RecipeHandler(stack));
		}

		return Optional.empty();
	}

	List<ItemStack> disassemble();

	Optional<DisassemblyHandler> insertIntoDisassemblySlot(ItemStack stack);

	void clearResults();

	DisassemblyHandler removeItemFromDeconstructionSlot();

	DisassemblyHandler removeItemFromResultSlot();

	void clear();
}
