package com.sigmundgranaas.forgero.minecraft.common.toolhandler.block.selector;

import java.util.Set;

import org.jetbrains.annotations.NotNull;

import net.minecraft.util.math.BlockPos;

/**
 * An interface for selecting specific block based on a root position.
 * <p>
 * Useful for selecting blocks in a pattern, or selecting blocks in a radius.
 * Used by the Pattern breaking features as well as the Vein mining feature.
 * <p>
 * Can be used bot for selecting blocks as well as filtering them
 */
public interface BlockSelector {

	/**
	 * @param rootPos the root position of the selection
	 * @return a set of all the blocks that are valid and in the radius around the root position.
	 * Will return an empty set if the root block is not valid
	 */
	@NotNull
	Set<BlockPos> select(BlockPos rootPos);
}
