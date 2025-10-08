package com.sigmundgranaas.forgero.properties.minecraft.blockbreaking;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.loader.api.ForgeroApi;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The central manager for providing Forgero's custom block breaking logic.
 * This class serves as the main entry point for the various block-breaking mixins.
 */
public class BlockBreakingManager {
	private static final Cache<CacheKey, Optional<BlockBreakingResult>> CACHE = Caffeine.newBuilder()
			.expireAfterAccess(Duration.of(20, ChronoUnit.SECONDS))
			.build();

	private BlockBreakingManager() {
		// Static class
	}

	/**
	 * Retrieves the block breaking result for a given player and position.
	 * Results are cached to improve performance during repeated calls.
	 */
	public static Optional<BlockBreakingResult> getBreakingResult(PlayerEntity player, BlockPos pos) {
		ItemStack stack = player.getMainHandStack();
		if (stack.isEmpty()) {
			return Optional.empty();
		}

		CacheKey key = new CacheKey(stack.getItem(), pos, Direction.getEntityFacingOrder(player)[0]);

		return CACHE.get(key, k -> calculateBreakingResult(player, pos));
	}

	/**
	 * Invalidates the cache for a specific breaking action.
	 */
	public static void clearCache(PlayerEntity player, BlockPos pos) {
		CacheKey key = new CacheKey(player.getMainHandStack().getItem(), pos, Direction.getEntityFacingOrder(player)[0]);
		CACHE.invalidate(key);
	}

	private static Optional<BlockBreakingResult> calculateBreakingResult(PlayerEntity player, BlockPos pos) {
		return ForgeroApi.converter().toComponent(player.getMainHandStack())
				.flatMap(component -> findActiveProperty(component, player, pos)
						.flatMap(property -> createResult(property, player, pos)));
	}

	private static Optional<BlockBreakingProperty> findActiveProperty(Component component, PlayerEntity player, BlockPos pos) {
		var engine = new BlockBreakingProperty.Engine();
		List<BlockBreakingProperty> bakedResult = ForgeroApi.resolver().resolve(component, engine);
		return bakedResult.stream().findFirst();
	}

	private static Optional<BlockBreakingResult> createResult(BlockBreakingProperty property, PlayerEntity player, BlockPos pos) {
		Set<BlockPos> selectedBlocks = property.selector().select(pos, player);
		if (selectedBlocks.isEmpty()) {
			return Optional.empty();
		}
		float speed = property.speed().calculateBlockBreakingDelta(player, pos, selectedBlocks);
		return Optional.of(new BlockBreakingResult(pos, selectedBlocks, speed));
	}

	/**
	 * Represents the result of a block breaking calculation.
	 */
	public record BlockBreakingResult(BlockPos origin, Set<BlockPos> blocks, float speed) {
		public Set<BlockPos> getAoe() {
			return blocks.stream().filter(p -> !p.equals(origin)).collect(Collectors.toUnmodifiableSet());
		}
	}

	private record CacheKey(Item item, BlockPos pos, Direction direction) {
	}
}
