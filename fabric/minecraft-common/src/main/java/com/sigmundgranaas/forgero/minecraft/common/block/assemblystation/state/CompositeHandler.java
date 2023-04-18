package com.sigmundgranaas.forgero.minecraft.common.block.assemblystation.state;

import java.util.List;
import java.util.Optional;

import com.sigmundgranaas.forgero.core.state.Composite;
import com.sigmundgranaas.forgero.minecraft.common.conversion.StateConverter;

import net.minecraft.item.ItemStack;

public class CompositeHandler implements DisassemblyHandler {
	private final Composite composite;

	public CompositeHandler(Composite composite) {
		this.composite = composite;
	}

	@Override
	public List<ItemStack> disassemble() {
		return composite.components().stream()
				.filter(state -> !state.identifier().contains("schematic"))
				.map(StateConverter::of)
				.toList();
	}

	public boolean isDisassembled(List<ItemStack> stacks) {
		var compared = disassemble();
		if (compared.size() != stacks.size()) {
			return false;
		}
		for (int i = 0; i < stacks.size(); i++) {
			if (!stacks.get(i).isItemEqualIgnoreDamage(compared.get(i))) {
				return false;
			}
		}
		return true;
	}

	@Override
	public Optional<DisassemblyHandler> insertIntoDisassemblySlot(ItemStack stack) {
		return DisassemblyHandler.createHandler(stack);
	}

	@Override
	public void clearResults() {

	}

	@Override
	public DisassemblyHandler removeItemFromDeconstructionSlot() {

	}

	@Override
	public DisassemblyHandler removeItemFromResultSlot() {

	}

	@Override
	public void clear() {

	}
}
