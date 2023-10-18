package com.sigmundgranaas.forgero.minecraft.common.handler.blockbreak.filter;

import javax.swing.text.html.BlockView;

import net.minecraft.util.math.BlockPos;

@FunctionalInterface
public interface BlockFilter {
	boolean filter(BlockView view, BlockPos pos);
}
