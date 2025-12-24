package com.sigmundgranaas.forgero.effects.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

/**
 * Ignites blocks at and around the hit position by placing fire.
 * Creates fire on burnable blocks or replaces air with fire blocks.
 *
 * <h3>JSON Configuration Example:</h3>
 * <pre>
 * {
 *   "type": "forgero:ignite_block",
 *   "radius": 2,
 *   "only_ignite_burnable": true
 * }
 * </pre>
 *
 * <h3>Behavior:</h3>
 * <ul>
 *   <li>Attempts to place fire at hit position and surrounding blocks within radius</li>
 *   <li>If only_ignite_burnable is true, only places fire on burnable materials</li>
 *   <li>Respects game rules and block flammability</li>
 *   <li>Will not place fire underwater or in invalid positions</li>
 * </ul>
 *
 * <h3>Performance Note:</h3>
 * Area ignition (radius > 0) checks multiple blocks. Use radius sparingly
 * on high-frequency effects. Radius 0-1 is cheap, 2-3 moderate, 4+ expensive.
 */
public record IgniteBlockEffect(
		int radius,
		boolean onlyIgniteBurnable
) implements OnHitBlockEffect {
	public static final String TYPE = "forgero:ignite_block";

	public static final Codec<IgniteBlockEffect> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.optionalFieldOf("radius", 0).forGetter(IgniteBlockEffect::radius),
			Codec.BOOL.optionalFieldOf("only_ignite_burnable", true).forGetter(IgniteBlockEffect::onlyIgniteBurnable)
	).apply(instance, IgniteBlockEffect::new));

	@Override
	public void apply(World world, Entity source, BlockPos pos) {
		if (world.isClient()) {
			return;
		}

		// Ignite center position
		ignitePosition(world, pos);

		// Ignite surrounding positions if radius > 0
		if (radius > 0) {
			for (BlockPos surroundingPos : BlockPos.iterateOutwards(pos, radius, radius, radius)) {
				if (!surroundingPos.equals(pos)) {
					ignitePosition(world, surroundingPos);
				}
			}
		}
	}

	private void ignitePosition(World world, BlockPos pos) {
		BlockState state = world.getBlockState(pos);

		// Try to place fire on top of the block if it's burnable
		if (state.isBurnable()) {
			BlockPos abovePos = pos.up();
			if (canPlaceFireAt(world, abovePos)) {
				world.setBlockState(abovePos, AbstractFireBlock.getState(world, abovePos), 11);
			}
			return;
		}

		// If not checking burnable status, or if block is air, place fire directly
		if (!onlyIgniteBurnable || state.isAir()) {
			if (canPlaceFireAt(world, pos)) {
				world.setBlockState(pos, AbstractFireBlock.getState(world, pos), 11);
			}
		}

		// Try adjacent positions for fire placement
		for (Direction direction : Direction.values()) {
			BlockPos adjacentPos = pos.offset(direction);
			BlockState adjacentState = world.getBlockState(adjacentPos);

			if (adjacentState.isAir() && canPlaceFireAt(world, adjacentPos)) {
				// Check if any neighbor of the air block is burnable
				boolean hasBurnableNeighbor = false;
				for (Direction checkDir : Direction.values()) {
					if (world.getBlockState(adjacentPos.offset(checkDir)).isBurnable()) {
						hasBurnableNeighbor = true;
						break;
					}
				}

				if (hasBurnableNeighbor || !onlyIgniteBurnable) {
					world.setBlockState(adjacentPos, AbstractFireBlock.getState(world, adjacentPos), 11);
					return; // Only place one fire per position to avoid spam
				}
			}
		}
	}

	private boolean canPlaceFireAt(World world, BlockPos pos) {
		// Check if fire can be placed at this position
		if (!world.canSetBlock(pos)) {
			return false;
		}

		BlockState state = world.getBlockState(pos);

		// Can only place fire in air or replace existing fire
		if (!state.isAir() && state.getBlock() != Blocks.FIRE && state.getBlock() != Blocks.SOUL_FIRE) {
			return false;
		}

		// Fire block has its own canPlaceAt logic we should respect
		BlockState fireState = AbstractFireBlock.getState(world, pos);
		return fireState.canPlaceAt(world, pos);
	}

	@Override
	public String type() {
		return TYPE;
	}
}
