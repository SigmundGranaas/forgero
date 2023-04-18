package com.sigmundgranaas.forgero.minecraft.common.block.assemblystation.state;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import net.minecraft.item.ItemStack;

public class EmptyHandler implements DisassemblyHandler {
	@Override
	public List<ItemStack> disassemble() {
		return Collections.emptyList();
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
		return this;
	}

	@Override
	public DisassemblyHandler removeItemFromResultSlot() {
		return this;
	}

	@Override
	public void clear() {

	}
}
