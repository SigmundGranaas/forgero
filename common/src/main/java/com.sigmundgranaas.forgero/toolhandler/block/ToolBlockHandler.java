package com.sigmundgranaas.forgero.toolhandler.block;

import static com.sigmundgranaas.forgero.match.MinecraftContextKeys.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableSet;
import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.property.v2.cache.CacheAbleKey;
import com.sigmundgranaas.forgero.core.property.v2.cache.FeatureCache;
import com.sigmundgranaas.forgero.core.property.v2.cache.FeatureContainerKey;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;
import com.sigmundgranaas.forgero.feature.BlockBreakFeature;
import lombok.Getter;
import lombok.experimental.Accessors;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

/**
 * A handler for a block that is being mined.
 * Will handle multiple blocks if the tool has the correct properties.
 * Prefer cached handlers from {@link BlockHandlerCache} over creating new ones.
 * Calculating the blocks to mine is expensive, and should only be done once per block unless it is invalidated.
 * Caching is extremely short term, and should only be for actions that calls the handler multiple times in a row, like when calculating brok breaking deltas.
 */
@Getter
@Accessors(fluent = true)
public class ToolBlockHandler {
	private static final Cache<CacheAbleKey, ToolBlockHandler> blockHandlerCache = CacheBuilder.newBuilder()
			.maximumSize(10)
			.softValues()
			.build();
	private static final Map<CacheAbleKey, Boolean> canMineCache = new HashMap<>();

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
		Item item = player.getMainHandStack().getItem();
		if (!item.isDamageable()) {
			return Optional.empty();
		}

		FeatureContainerKey featureKey = FeatureContainerKey.of(container, BlockBreakFeature.KEY);
		CacheAbleKey cacheKey = new Key(player.getMainHandStack(), pos, Direction.getEntityFacingOrder(player)[0]);
		if (!FeatureCache.check(featureKey) || (canMineCache.containsKey(cacheKey) && !canMineCache.get(cacheKey))) {
			return Optional.empty();
		}

		ToolBlockHandler cachedHandler = blockHandlerCache.getIfPresent(cacheKey);

		if (cachedHandler != null) {
			return Optional.of(cachedHandler);
		}

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
			var handler = new ToolBlockHandler(pos, selected, featureOpt.get().calculateBlockBreakingDelta(player, pos, selected), cacheKey);
			blockHandlerCache.put(cacheKey, handler);
			return Optional.of(handler);
		} else {
			canMineCache.put(cacheKey, false);
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
		BlockHandlerCache.remove(key);
		canMineCache.clear();
	}


	public record Key(ItemStack stack, BlockPos pos, Direction direction) implements CacheAbleKey {

		@Override
		public String key() {
			return stack.hashCode() + ":" + pos.asLong() + ":" + direction.toString();
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			Key key = (Key) o;
			return key().equals(key.key());
		}

		@Override
		public int hashCode() {
			return Objects.hash(key());
		}
	}
}
