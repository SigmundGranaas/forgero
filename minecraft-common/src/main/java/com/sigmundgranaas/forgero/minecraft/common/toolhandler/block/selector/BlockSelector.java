package com.sigmundgranaas.forgero.minecraft.common.toolhandler.block.selector;

import static com.sigmundgranaas.forgero.minecraft.common.toolhandler.block.BlockUtils.*;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import org.jetbrains.annotations.NotNull;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;

/**
 * An interface for selecting specific block based on a root position.
 * <p>
 * Useful for selecting blocks in a pattern, or selecting blocks in a radius.
 * Used by the Pattern breaking features as well as the Vein mining feature.
 * <p>
 * Can be used both for selecting blocks and filtering them
 */
public interface BlockSelector {

	BlockSelector EMPTY = rootPos -> Set.of();

	static BlockSelector of(List<String> pattern, PlayerEntity player) {
		var patternBlockSelector = new PatternBlockSelectionStrategy(pattern, Direction.getEntityFacingOrder(player), player.getHorizontalFacing());
		return CachedSelector.of(patternBlockSelector);
	}

	static BlockSelector of(List<String> pattern, PlayerEntity player, List<String> tags) {
		Predicate<BlockPos> filter = isInTags(player.world, tags);
		var selector = new PatternBlockSelectionStrategy(pattern, Direction.getEntityFacingOrder(player), player.getHorizontalFacing());
		var filteredSelector = new FilteredSelector(selector, filter);
		return CachedSelector.of(filteredSelector);
	}

	static BlockSelector of(int depth, BlockView view, PlayerEntity player, List<String> tags) {
		Predicate<BlockPos> filter = isBreakableBlock(view)
				.and(canHarvest(view, player))
				.and(isInTags(view, tags));
		var selector = new RadiusVeinMiningBlockSelectionStrategy(depth, filter);
		return CachedSelector.of(selector);
	}


	/**
	 * @param rootPos the root position of the selection
	 * @return a set of all the blocks that are valid and in the radius around the root position.
	 * Will return an empty set if the root block is not valid
	 */
	@NotNull
	Set<BlockPos> select(BlockPos rootPos);
}
