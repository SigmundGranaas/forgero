package com.sigmundgranaas.forgero.fabric.block.assemblystation.state;

import java.util.Collections;
import java.util.List;

import net.minecraft.item.ItemStack;

public class EmptyHandler implements DisassemblyHandler {
	@Override
	public List<ItemStack> disassemble() {
		return Collections.emptyList();
	}

	@Override
	public DisassemblyHandler insertIntoDisassemblySlot(ItemStack stack) {
		return createHandler(stack);
	}
}
