package com.sigmundgranaas.forgero.minecraft.common.block.assemblystation.state;

import java.util.List;

import com.sigmundgranaas.forgero.core.state.Composite;
import com.sigmundgranaas.forgero.minecraft.common.resources.DisassemblyRecipeLoader;
import com.sigmundgranaas.forgero.minecraft.common.service.StateService;

import net.minecraft.item.ItemStack;

public interface DisassemblyHandler {
	static DisassemblyHandler createHandler(ItemStack stack) {
		boolean noDamage = stack.getDamage() == 0;
		var recipe = DisassemblyRecipeLoader.getEntries().stream()
				.filter(entry -> entry.getInput().test(stack))
				.findFirst();
		if (noDamage && StateService.INSTANCE.convert(stack).isPresent() && StateService.INSTANCE.convert(stack).get() instanceof Composite composite) {
			return new CompositeHandler(composite);
		} else if (noDamage && recipe.isPresent()) {
			return new RecipeHandler(recipe.get());
		}

		return new EmptyHandler();
	}

	List<ItemStack> disassemble();

	DisassemblyHandler insertIntoDisassemblySlot(ItemStack stack);

	default boolean isDisassembled(List<ItemStack> stacks) {
		var items2 = disassemble();
		if (stacks.size() != items2.size()) {
			return false;
		}
		for (int i = 0; i < stacks.size(); i++) {
			if (!stacks.get(i).isOf(items2.get(i).getItem())) {
				return false;
			}
		}
		return true;
	}
}
