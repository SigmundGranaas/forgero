package com.sigmundgranaas.forgero.minecraft.common.toolhandler.block;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.property.v2.cache.CacheAbleKey;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

/**
 * A handler for a block that is being mined.
 * Will handle multiple blocks if the tool has the correct properties.
 * Prefer cached handlers from {@link BlockHandlerCache} over creating new ones.
 * Calculating the blocks to mine is expensive, and should only be done once per block unless it is invalidated.
 * Caching is extremely short term, and should only be for actions that calls the handler multiple times in a row, like when calculating brok breaking deltas.
 * See {@link BlockHandlerFactory} for creating handlers from tools.
 */
public class ToolBlockHandler {
	public static ToolBlockHandler EMPTY = new ToolBlockHandler(new BlockPos(0, 0, 0), Collections.emptySet(), 0f, CacheAbleKey.EMPTY);
	public static String BLOCK_BREAKING_PATTERN_KEY = "BLOCK_BREAKING_PATTERN";
	public static String VEIN_MINING_KEY = "VEIN_MINING";
	public static String COLUMN_MINING_KEY = "COLUMN_MINING";

	public static Set<String> BLOCK_BREAKING_KEYS = Set.of(BLOCK_BREAKING_PATTERN_KEY, VEIN_MINING_KEY, COLUMN_MINING_KEY);

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
		return BlockHandlerFactory.create(container, world, pos, player);
	}

	public float getHardness() {
		return hardness;
	}

	/**
	 * Runs an action for every block in the handler.
	 *
	 * @param consumer the action to run. Can be a call to break a block. Or a call for custom xp handlers.
	 * @return the handler for chaining.
	 */
	public ToolBlockHandler handle(Consumer<BlockPos> consumer) {
		availableBlocks.forEach(consumer);
		return this;
	}

	/**
	 * Runs an action for every block in the handler except the origin block.
	 * This is useful when you want to break the origin block last, or if the origin block is handled by another system.
	 *
	 * @param consumer the action to run. Can be a call to break a block. Or a call for custom xp handlers.
	 * @return the handler for chaining.
	 */
	public ToolBlockHandler handleExceptOrigin(Consumer<BlockPos> consumer) {
		availableBlocks.stream().filter(info -> !info.equals(originPos)).forEach(consumer);
		return this;
	}

	/**
	 * Cleans up the handler after it has been used.
	 * This will remove the handled selection from the cache, preventing invalid selections results.
	 */
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
