package com.sigmundgranaas.forgero.minecraft.common.selector;

import static net.minecraft.util.registry.Registry.BLOCK_KEY;

import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.sigmundgranaas.forgero.core.Forgero;
import org.jetbrains.annotations.NotNull;

import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;

/**
 * Class for filtering a block selector based on a blockstate predicate
 * <p>
 * Can be used to filter blocks based on if a player can harvest the block, or if the block is in a specific tag
 */
public class FilterSelector implements BlockSelector {
	private final BlockSelector blockFinder;
	private final Predicate<BlockPos> blockFilter;

	public FilterSelector(BlockSelector blockFinder, Predicate<BlockPos> blockFilter) {
		this.blockFinder = blockFinder;
		this.blockFilter = blockFilter;
	}

	/**
	 * @return BlockSelector that filters out blocks that the player cannot harvest
	 */
	@NotNull
	public static BlockSelector canPlayerHarvest(WorldView world, PlayerEntity player, BlockSelector blockFinder) {
		return new FilterSelector(blockFinder, pos -> player.canHarvest(world.getBlockState(pos)));
	}

	/**
	 * @return BlockSelector that filters out blocks that are not in the tag
	 */
	@NotNull
	public static BlockSelector isTaggedBlock(BlockSelector blockFinder, WorldView view, TagKey<Block> tag) {
		return new FilterSelector(blockFinder, pos -> view.getBlockState(pos).isIn(tag));
	}

	/**
	 * @return BlockSelector that filters out blocks that are not in the tag
	 * Defaults to accept all blocks if the tag is not found
	 */
	@NotNull
	public static BlockSelector isTaggedBlock(BlockSelector blockFinder, WorldView view, String tag) {
		try {
			Identifier tagId = new Identifier(tag);
			TagKey<Block> tagKey = TagKey.of(BLOCK_KEY, tagId);
			return isTaggedBlock(blockFinder, view, tagKey);
		} catch (InvalidIdentifierException e) {
			Forgero.LOGGER.error("Invalid tag identifier used to create a block selector filter: " + tag);
			return new FilterSelector(blockFinder, pos -> true);
		}
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
