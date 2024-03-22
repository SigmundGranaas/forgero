package com.sigmundgranaas.forgero.testutil;

import java.util.List;

import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.state.composite.ConstructedTool;
import com.sigmundgranaas.forgero.minecraft.common.service.StateService;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;

public class Utils {
	public static ItemStack createTool(String headId, String handleId, String id) {
		State head = StateService.INSTANCE.find(headId).get();
		State handle = StateService.INSTANCE.find(handleId).get();

		State pickaxe = ConstructedTool.ToolBuilder.builder(List.of(head, handle))
				.get()
				.id(id)
				.build();

		return StateService.INSTANCE.convert(pickaxe).get();
	}


	public static String debugId(BlockState state) {
		return Registries.BLOCK.getId(state.getBlock()).toString();
	}
}
