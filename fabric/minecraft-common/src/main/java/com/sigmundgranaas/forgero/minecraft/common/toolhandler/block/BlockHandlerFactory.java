package com.sigmundgranaas.forgero.minecraft.common.toolhandler.block;

import static com.sigmundgranaas.forgero.minecraft.common.toolhandler.block.BlockUtils.canHarvest;
import static com.sigmundgranaas.forgero.minecraft.common.toolhandler.block.BlockUtils.isInTags;
import static com.sigmundgranaas.forgero.minecraft.common.toolhandler.block.ToolBlockHandler.*;

import java.util.Arrays;
import java.util.Optional;

import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.property.v2.cache.CacheAbleKey;
import com.sigmundgranaas.forgero.core.property.v2.cache.ContainsFeatureCache;
import com.sigmundgranaas.forgero.core.property.v2.cache.PropertyTargetCacheKey;
import com.sigmundgranaas.forgero.core.property.v2.feature.PropertyData;
import com.sigmundgranaas.forgero.minecraft.common.toolhandler.block.hardness.HardnessProvider;
import com.sigmundgranaas.forgero.minecraft.common.toolhandler.block.selector.BlockSelector;

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
		return Optional.of(handler);
	}

	/**
	 * Creates a handler for the given block position and the properties of the tool.
	 *
	 * @param pos the root position of the block
	 * @param key the key to use for caching the handler
	 * @return the handler for the given block position and the properties of the tool
	 */
	public ToolBlockHandler createHandler(BlockPos pos, CacheAbleKey key) {
		PropertyData data = data();
		BlockSelector selector = selector(data);
		HardnessProvider hardness = HardnessProvider.of(view, player, selector);
		return new ToolBlockHandler(pos, selector.select(pos), hardness.getHardnessAt(pos), key);
	}

	private PropertyData data() {
		var blockMiningData = container.stream().features().filter(data -> BLOCK_BREAKING_KEYS.contains(data.type())).toList();
		return blockMiningData.get(0);
	}

	/**
	 * Creates a {@link BlockSelector} based on the properties of the tool.
	 *
	 * @param data the property data used to determine which selector to create.
	 * @return A selector for all valid blocks.
	 */
	private BlockSelector selector(PropertyData data) {
		BlockSelector selector = BlockSelector.EMPTY;
		if (data.getPattern() != null && data.isOfType(BLOCK_BREAKING_PATTERN_KEY)) {
			var patternSelector = BlockSelector.of(Arrays.asList(data.getPattern()), player);
			selector = patternSelector.filter(canHarvest(view, player));
			if (!data.getTags().isEmpty()) {
				selector = selector.filter(isInTags(view, data.getTags()));
			}
		} else if (data.isOfType(VEIN_MINING_KEY) && data.getTags() != null) {
			selector = BlockSelector.of((int) data.getValue(), view, player, data.getTags());
		} else if (data.isOfType(COLUMN_MINING_KEY)) {
			selector = BlockSelector.of((int) data.getValue(), data.getLevel(), view, player, data.getTags());
		}

		return selector;
	}
}
