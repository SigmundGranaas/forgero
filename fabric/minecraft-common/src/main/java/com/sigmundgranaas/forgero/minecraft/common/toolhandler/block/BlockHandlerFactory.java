package com.sigmundgranaas.forgero.minecraft.common.toolhandler.block;

import static com.sigmundgranaas.forgero.minecraft.common.toolhandler.block.ToolBlockHandler.BLOCK_BREAKING_KEYS;

import java.util.Optional;

import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.property.v2.cache.CacheAbleKey;
import com.sigmundgranaas.forgero.core.property.v2.cache.ContainsFeatureCache;
import com.sigmundgranaas.forgero.core.property.v2.cache.PropertyTargetCacheKey;
import com.sigmundgranaas.forgero.minecraft.common.handler.blockbreak.hardness.HardnessProvider;
import com.sigmundgranaas.forgero.minecraft.common.handler.blockbreak.selector.BlockSelector;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;

/**
 * Factory for creating {@link ToolBlockHandler}s.
 * Will produce the correct handler based on the properties of the tool.
 * Can produce handlers for vein mining, column mining and block breaking patterns.
 * This class will use the {@link BlockHandlerCache} to cache the created handlers.
 */
public class BlockHandlerFactory {
	private final PropertyContainer container;
	private final BlockView view;
	private final PlayerEntity player;

	public BlockHandlerFactory(PropertyContainer container, BlockView view, PlayerEntity player) {
		this.container = container;
		this.view = view;
		this.player = player;
	}

	/**
	 * Creates a handler for the given block position and the properties of the tool.
	 *
	 * @return the handler for the given block position and the properties of the tool
	 */
	public static Optional<ToolBlockHandler> create(PropertyContainer container, BlockView view, BlockPos pos, PlayerEntity player) {
		if (!ContainsFeatureCache.check(BLOCK_BREAKING_KEYS, (key) -> PropertyTargetCacheKey.of(container, key))) {
			return Optional.empty();
		}
		if (!player.canHarvest(view.getBlockState(pos))) {
			return Optional.empty();
		}
		var key = new BlockHandlerCache.BlockStateCacheKey(new ToolBlockHandler.BlockInfo(pos), container, Direction.getEntityFacingOrder(player));
		var handler = BlockHandlerCache.computeIfAbsent(key, () -> new BlockHandlerFactory(container, view, player).createHandler(pos, key));
		return Optional.of(handler).filter(h -> h.getAvailableBlocks().size() > 1);
	}

	/**
	 * Creates a handler for the given block position and the properties of the tool.
	 *
	 * @param pos the root position of the block
	 * @param key the key to use for caching the handler
	 * @return the handler for the given block position and the properties of the tool
	 */
	public ToolBlockHandler createHandler(BlockPos pos, CacheAbleKey key) {
		Object data = null;
		BlockSelector selector = null;
		HardnessProvider hardness = HardnessProvider.of(view, player, selector);
		return new ToolBlockHandler(pos, selector.select(pos, null), hardness.getHardnessAt(pos), key);
	}
}
