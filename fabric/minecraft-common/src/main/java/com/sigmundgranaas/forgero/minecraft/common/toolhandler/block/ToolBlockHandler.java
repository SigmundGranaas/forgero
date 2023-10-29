package com.sigmundgranaas.forgero.minecraft.common.toolhandler.block;

import static com.sigmundgranaas.forgero.minecraft.common.match.MinecraftContextKeys.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import com.google.common.collect.ImmutableSet;
import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.property.v2.cache.CacheAbleKey;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;
import com.sigmundgranaas.forgero.minecraft.common.feature.BlockBreakFeature;
import lombok.Getter;
import lombok.experimental.Accessors;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

/**
 * A handler for a block that is being mined.
 * Will handle multiple blocks if the tool has the correct properties.
 * Prefer cached handlers from {@link BlockHandlerCache} over creating new ones.
 * Calculating the blocks to mine is expensive, and should only be done once per block unless it is invalidated.
 * Caching is extremely short term, and should only be for actions that calls the handler multiple times in a row, like when calculating brok breaking deltas.
 * See {@link BlockHandlerHelper} for creating handlers from tools.
 */
@Getter
@Accessors(fluent = true)
public class ToolBlockHandler {
	public static ToolBlockHandler EMPTY = new ToolBlockHandler(new BlockPos(0, 0, 0), Collections.emptySet(), 0f, CacheAbleKey.EMPTY);

	private final Set<BlockPos> availableBlocks;
	private final BlockPos originPos;
	private final float hardness;
	private final CacheAbleKey key;

	public ToolBlockHandler(BlockPos originPos, Set<BlockPos> blocks, float hardness, CacheAbleKey key) {
		this.key = key;
		this.availableBlocks = new HashSet<>(blocks);
		this.originPos = originPos;
		this.hardness = hardness;
	}

	public static Optional<ToolBlockHandler> of(PropertyContainer container, BlockPos pos, PlayerEntity player) {
		MatchContext ctx = MatchContext.of()
				.put(WORLD, player.getWorld())
				.put(ENTITY, player)
				.put(BLOCK_TARGET, pos);


		var featureOpt = container.stream()
				.features(BlockBreakFeature.KEY)
				.filter(feature -> feature.test(Matchable.DEFAULT_TRUE, ctx))
				.findFirst();

		if (featureOpt.isPresent()) {
			Set<BlockPos> selected = ImmutableSet.copyOf(featureOpt.get().selectBlocks(player, pos));
			return Optional.of(new ToolBlockHandler(pos, selected, featureOpt.get().calculateBlockBreakingDelta(player, pos, selected), CacheAbleKey.EMPTY));
		}
		return Optional.empty();
	}

	/**
	 * Runs an action for every block in the handler.
	 *
	 * @param consumer the action to run. Can be a call to break a block. Or a call for custom xp handlers.
	 * @return the handler for chaining.
	 */
	public ToolBlockHandler handle(Consumer<BlockPos> consumer) {
		for (BlockPos blockPos : availableBlocks) {
			consumer.accept(blockPos);
		}
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
		for (BlockPos blockPos : availableBlocks) {
			if (!blockPos.equals(originPos)) {
				consumer.accept(blockPos);
			}
		}
		return this;
	}

	/**
	 * Cleans up the handler after it has been used.
	 * This will remove the handled selection from the cache, preventing invalid selections results.
	 */
	public void cleanUp() {
		//BlockHandlerCache.remove(key);
	}

	public record BlockInfo(BlockPos pos) implements CacheAbleKey {
		@Override
		public String key() {
			return pos.asLong() + "";
		}
	}
}
