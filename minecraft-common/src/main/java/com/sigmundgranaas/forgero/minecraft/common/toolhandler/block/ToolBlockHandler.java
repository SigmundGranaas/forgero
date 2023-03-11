package com.sigmundgranaas.forgero.minecraft.common.toolhandler.block;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.property.v2.cache.CacheAbleKey;
import com.sigmundgranaas.forgero.core.property.v2.cache.ContainsFeatureCache;
import com.sigmundgranaas.forgero.core.property.v2.cache.PropertyTargetCacheKey;
import com.sigmundgranaas.forgero.minecraft.common.toolhandler.block.hardness.HardnessProvider;
import com.sigmundgranaas.forgero.minecraft.common.toolhandler.block.selector.BlockSelector;
import com.sigmundgranaas.forgero.minecraft.common.toolhandler.block.selector.FilteredSelector;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;

public class ToolBlockHandler {

	public static ToolBlockHandler EMPTY = new ToolBlockHandler(new BlockPos(0, 0, 0), Collections.emptySet(), 0f, CacheAbleKey.EMPTY);
	public static String BLOCK_BREAKING_PATTERN_KEY = "BLOCK_BREAKING_PATTERN";
	public static String VEIN_MINING_KEY = "VEIN_MINING";
	public static String COLUMN_MINING_KEY = "COLUMN_MINING";

	private final Set<BlockPos> availableBlocks;
	private final BlockPos originPos;
	private final float hardness;
	private final CacheAbleKey key;

	public ToolBlockHandler(BlockPos originPos, Set<BlockPos> blocks, float hardness, CacheAbleKey key) {
		this.key = key;
		this.availableBlocks = blocks;
		this.originPos = originPos;
		this.hardness = hardness;
	}

	public static Optional<ToolBlockHandler> of(PropertyContainer container, BlockView world, BlockPos pos, PlayerEntity player) {
		boolean has = ContainsFeatureCache.check(PropertyTargetCacheKey.of(container, BLOCK_BREAKING_PATTERN_KEY)) || ContainsFeatureCache.check(PropertyTargetCacheKey.of(container, VEIN_MINING_KEY));
		if (has) {
			var key = new BlockHandlerCache.BlockStateCacheKey(new BlockInfo(pos), container, Direction.getEntityFacingOrder(player));
			var handler = BlockHandlerCache.computeIfAbsent(key, () -> createHandler(container, world, pos, player, key));
			return Optional.of(handler);
		}
		return Optional.empty();
	}

	public static ToolBlockHandler createHandler(PropertyContainer container, BlockView world, BlockPos pos, PlayerEntity player, CacheAbleKey key) {
		var blockMiningData = container.stream().features().filter(data -> data.type().equals(BLOCK_BREAKING_PATTERN_KEY) || data.type().equals(VEIN_MINING_KEY)).toList();
		var data = blockMiningData.get(0);
		BlockSelector selector;
		if (data.getPattern() != null) {
			var patternSelector = BlockSelector.of(Arrays.asList(data.getPattern()), player);
			selector = FilteredSelector.canPlayerHarvest(world, player, patternSelector);
		} else {
			selector = BlockSelector.of((int) data.getValue(), world, player, data.getTags());
		}

		HardnessProvider hardnessProvider = HardnessProvider.of(world, player, selector);
		return new ToolBlockHandler(pos, selector.select(pos), hardnessProvider.hardness(pos), key);
	}

	public float getHardness() {
		return hardness;
	}

	public ToolBlockHandler handle(Consumer<BlockPos> consumer) {
		availableBlocks.forEach(consumer);
		return this;
	}

	public ToolBlockHandler handleExceptOrigin(Consumer<BlockPos> consumer) {
		availableBlocks.stream().filter(info -> !info.equals(originPos)).forEach(consumer);
		return this;
	}

	public void cleanUp() {
		BlockHandlerCache.remove(key);
	}

	public record BlockInfo(BlockPos pos) implements CacheAbleKey {
		@Override
		public String key() {
			return pos.asLong() + "";
		}
	}
}
