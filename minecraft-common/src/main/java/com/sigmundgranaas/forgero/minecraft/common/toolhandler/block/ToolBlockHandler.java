package com.sigmundgranaas.forgero.minecraft.common.toolhandler.block;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Consumer;

import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.property.v2.cache.CacheAbleKey;
import com.sigmundgranaas.forgero.core.property.v2.cache.ContainsFeatureCache;
import com.sigmundgranaas.forgero.core.property.v2.cache.PropertyTargetCacheKey;
import com.sigmundgranaas.forgero.minecraft.common.toolhandler.VeinMiningStrategy;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;

public class ToolBlockHandler {

	public static ToolBlockHandler EMPTY = new ToolBlockHandler(new TreeMap<>(), new BlockPos(0, 0, 0), 0f, CacheAbleKey.EMPTY);
	public static String BLOCK_BREAKING_PATTERN_KEY = "BLOCK_BREAKING_PATTERN";
	public static String VEIN_MINING_KEY = "VEIN_MINING";

	private final List<BlockInfo> availableBlocks;
	private final BlockPos originPos;
	private final float hardness;
	private final CacheAbleKey key;

	public ToolBlockHandler(SortedMap<BlockPos, BlockState> availableBlocks, BlockPos originPos, float hardness, CacheAbleKey key) {
		this.key = key;
		this.availableBlocks = new ArrayList<>();
		availableBlocks.forEach((blockPos, blockState) -> {
			this.availableBlocks.add(new BlockInfo(blockPos, blockState));
		});

		this.originPos = originPos;
		this.hardness = hardness;
	}

	public static Optional<ToolBlockHandler> of(PropertyContainer container, BlockView world, BlockPos pos, PlayerEntity player) {
		boolean has = ContainsFeatureCache.check(PropertyTargetCacheKey.of(container, BLOCK_BREAKING_PATTERN_KEY)) || ContainsFeatureCache.check(PropertyTargetCacheKey.of(container, VEIN_MINING_KEY));
		if (has) {
			var key = new BlockHandlerCache.BlockStateCacheKey(new BlockInfo(pos, world.getBlockState(pos)), container, Direction.getEntityFacingOrder(player));
			var handler = BlockHandlerCache.computeIfAbsent(key, () -> createHandler(container, world, pos, player, key));
			return Optional.of(handler);
		}
		return Optional.empty();
	}

	public static ToolBlockHandler createHandler(PropertyContainer container, BlockView world, BlockPos pos, PlayerEntity player, CacheAbleKey key) {
		var blockMiningData = container.stream().features().filter(data -> data.type().equals(BLOCK_BREAKING_PATTERN_KEY) || data.type().equals(VEIN_MINING_KEY)).toList();
		BlockBreakingHandler handler;
		var data = blockMiningData.get(0);

		if (data.type().equals(BLOCK_BREAKING_PATTERN_KEY)) {
			handler = new BlockBreakingHandler(new PatternBreakingStrategy(data));
		} else {
			handler = new BlockBreakingHandler(new VeinMiningStrategy(data));
		}

		var availableBlocks = handler.getAvailableBlocks(world, pos, player);
		var availableBlocksStates = new TreeMap<BlockPos, BlockState>();

		availableBlocks.forEach(blockPos -> {
			availableBlocksStates.put(blockPos, world.getBlockState(blockPos));
		});

		return new ToolBlockHandler(availableBlocksStates, pos, handler.getHardness(world.getBlockState(pos), pos, world, player), key);
	}

	public float getHardness() {
		return hardness;
	}

	public ToolBlockHandler handle(Consumer<BlockInfo> consumer) {
		availableBlocks.forEach(consumer);
		return this;
	}

	public ToolBlockHandler handleExceptOrigin(Consumer<BlockInfo> consumer) {
		availableBlocks.stream().filter(info -> !info.pos.equals(originPos)).forEach(consumer);
		return this;
	}

	public void cleanUp() {
		BlockHandlerCache.remove(key);
	}

	public record BlockInfo(BlockPos pos, BlockState state) implements CacheAbleKey {
		@Override
		public String key() {
			return pos.asLong() + state.toString();
		}
	}
}
