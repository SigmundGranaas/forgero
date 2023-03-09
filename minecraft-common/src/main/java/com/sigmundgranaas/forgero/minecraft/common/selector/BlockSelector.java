package com.sigmundgranaas.forgero.minecraft.common.selector;

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
	@NotNull
	Set<BlockPos> select(BlockPos rootPos);
}
