package com.sigmundgranaas.forgero.minecraft.common.toolhandler.block;

import static net.minecraft.util.registry.Registry.BLOCK_KEY;

import java.util.List;
import java.util.function.Predicate;

import com.sigmundgranaas.forgero.core.Forgero;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

/**
 * Utility class for working with blocks.
 */
public class BlockUtils {
	/**
	 * @param view   the world
	 * @param player the player that is harvesting the block
	 * @return a predicate that returns true if the player can harvest the block at the given position.
	 */
	public static Predicate<BlockPos> canHarvest(BlockView view, PlayerEntity player) {
		return (pos) -> player.canHarvest(view.getBlockState(pos));
	}

	/**
	 * @param view the world
	 * @return a predicate that returns true if the block at the given position is a breakable block.
	 */
	public static Predicate<BlockPos> isBreakableBlock(BlockView view) {
		return pos -> {
			BlockState state = view.getBlockState(pos);
			return !state.isAir() && !(state.getHardness(view, pos) < 0);
		};
	}

	/**
	 * @param view the world
	 * @param tag  the tag to check if the block is in
	 * @return a predicate that returns true if the block at the given position is in the given tag.
	 */
	public static Predicate<BlockPos> isInTag(BlockView view, TagKey<Block> tag) {
		return pos -> view.getBlockState(pos).isIn(tag);
	}

	/**
	 * @param view the world
	 * @param tag  the tag to check if the block is in
	 * @return a predicate that returns true if the block at the given position is in the given tag.
	 */
	public static Predicate<BlockPos> isInTag(BlockView view, String tag) {
		try {
			Identifier tagId = new Identifier(tag);
			TagKey<Block> tagKey = TagKey.of(BLOCK_KEY, tagId);
			return isInTag(view, tagKey);
		} catch (InvalidIdentifierException e) {
			Forgero.LOGGER.error("Invalid tag identifier used to create a block selector filter: " + tag);
			return pos -> false;
		}
	}

	/**
	 * @param view the world
	 * @param tags the tags to check if the block is in
	 * @return a predicate that returns true if the block at the given position is in any of the given tags.
	 */
	public static Predicate<BlockPos> isInTags(BlockView view, List<String> tags) {
		return pos -> tags.stream().anyMatch(tag -> isInTag(view, tag).test(pos));
	}

}
