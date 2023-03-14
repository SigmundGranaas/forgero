package com.sigmundgranaas.forgero.minecraft.common.toolhandler.block.selector;

import static com.sigmundgranaas.forgero.minecraft.common.toolhandler.block.BlockUtils.*;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;

import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tag.TagKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

/**
 * Class for filtering a block selector based on a block state predicate
 * <p>
 * Can be used to filter blocks based on if a player can harvest the block, or if the block is in a specific tag
 */
public class FilteredSelector implements BlockSelector {
	private final BlockSelector blockFinder;
	private final Predicate<BlockPos> blockFilter;

	public FilteredSelector(BlockSelector blockFinder, Predicate<BlockPos> blockFilter) {
		this.blockFinder = blockFinder;
		this.blockFilter = blockFilter;
	}

	/**
	 * @return BlockSelector that filters out blocks that the player cannot harvest
	 */
	@NotNull
	public static BlockSelector canPlayerHarvest(BlockView world, PlayerEntity player, BlockSelector blockFinder) {
		return new FilteredSelector(blockFinder, canHarvest(world, player));
	}


	/**
	 * @return BlockSelector that filters out blocks that are not in the tag
	 */
	@NotNull
	public static BlockSelector isTaggedBlock(BlockSelector blockFinder, BlockView view, TagKey<Block> tag) {
		return new FilteredSelector(blockFinder, isInTag(view, tag));
	}

	/**
	 * @return BlockSelector that filters out blocks that are not in the tag
	 * Defaults to remove blocks if the tag is not found
	 */
	@NotNull
	public static BlockSelector isTaggedBlock(BlockSelector blockFinder, BlockView view, String tag) {
		return new FilteredSelector(blockFinder, isInTag(view, tag));
	}

	/**
	 * @return BlockSelector that filters out blocks that are not in the tag
	 * Defaults to remove blocks if the tag is not found
	 */
	@NotNull
	public static BlockSelector isTaggedBlock(BlockSelector blockFinder, BlockView view, List<String> tags) {
		return new FilteredSelector(blockFinder, isInTags(view, tags));
	}

	/**
	 * Selects blocks based on the block selector and the block filter
	 *
	 * @param blockPos Origin for the block selector
	 * @return A filtered set of blocks
	 */
	@Override
	@NotNull
	public Set<BlockPos> select(BlockPos blockPos) {
		return blockFinder.select(blockPos)
				.stream()
				.filter(blockFilter)
				.collect(Collectors.toUnmodifiableSet());
	}
}
