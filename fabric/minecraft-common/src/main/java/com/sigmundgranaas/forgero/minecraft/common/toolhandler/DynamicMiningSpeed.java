package com.sigmundgranaas.forgero.minecraft.common.toolhandler;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;

public interface DynamicMiningSpeed {
	default float getMiningSpeedMultiplier(BlockState state, ItemStack stack) {
		return 1.0F;
	}
}
