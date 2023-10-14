package com.sigmundgranaas.forgero.minecraft.common.handler.blockbreak.hardness;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

/**
 * A hardness provider that returns the hardness of a single block.
 */
public class SingleBlockHardnessProvider implements HardnessProvider {
	private final BlockView view;

	public SingleBlockHardnessProvider(BlockView view) {
		this.view = view;
	}

	public static HardnessProvider of(BlockView view) {
		return new SingleBlockHardnessProvider(view);
	}

	@Override
	public float getHardnessAt(BlockPos pos) {
		return view.getBlockState(pos).getHardness(view, pos);
	}
}
