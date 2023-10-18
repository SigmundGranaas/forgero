package com.sigmundgranaas.forgero.minecraft.common.handler.blockbreak.selector;

import java.util.Set;

import org.jetbrains.annotations.NotNull;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

/**
 * Class for caching a block selector.
 * <p>
 * Can be used to cache a block selector to avoid recalculating the selection every time the selection is used.
 */
public class CachedSelector implements BlockSelector {
	private final BlockSelector blockFinder;
	private Set<BlockPos> cachedBlocks;

	public CachedSelector(BlockSelector blockFinder) {
		this.blockFinder = blockFinder;
	}

	public static BlockSelector of(BlockSelector blockFinder) {
		return new CachedSelector(blockFinder);
	}

	/**
	 * Cached the current block selection.
	 * Will only recalculate the selection if the cache is invalidated.
	 *
	 * @param rootPos the root position of the selection
	 * @return A set of cached blocks from the given selector.
	 */
	@Override
	@NotNull
	public Set<BlockPos> select(BlockPos rootPos, Entity source) {
		if (cachedBlocks == null || cachedBlocks.isEmpty()) {
			cachedBlocks = blockFinder.select(rootPos, source);
		}
		return cachedBlocks;
	}

	/**
	 * Invalidates the local cache.
	 */
	public void invalidateCache() {
		cachedBlocks.clear();
	}
}
