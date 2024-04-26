package com.sigmundgranaas.forgero.minecraft.common.block.assemblystation.state;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.sigmundgranaas.forgero.core.state.Composite;
import com.sigmundgranaas.forgero.core.type.Type;
import com.sigmundgranaas.forgero.minecraft.common.service.StateService;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class CompositeHandler implements DisassemblyHandler {
	private final Composite composite;

	public CompositeHandler(Composite composite) {
		this.composite = composite;
	}

	@Override
	public List<ItemStack> disassemble() {
		if (composite.test(Type.ARROW_HEAD)) {
			return Collections.emptyList();
		}
		if (composite.test(Type.ARROW)) {
			return parts()
					.filter(stack -> stack.getItem() != Items.FEATHER)
					.toList();
		}
		return parts().toList();
	}

	private Stream<ItemStack> parts() {
		return composite.components().stream()
				.filter(state -> !state.identifier().contains("schematic"))
				.map(StateService.INSTANCE::convert)
				.flatMap(Optional::stream);
	}


	@Override
	public DisassemblyHandler insertIntoDisassemblySlot(ItemStack stack) {
		return DisassemblyHandler.createHandler(stack);
	}
}
