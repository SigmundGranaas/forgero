package com.sigmundgranaas.forgero.effects.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

/**
 * Places a block at or near the hit position.
 * Useful for torch placement, block creation, or environmental modification.
 *
 * <h3>JSON Configuration Example:</h3>
 * <pre>
 * {
 *   "type": "forgero:place_block",
 *   "block": "minecraft:torch",
 *   "offset_direction": "source_facing",
 *   "replace_air_only": true
 * }
 * </pre>
 *
 * <h3>Offset Direction Options:</h3>
 * <ul>
 *   <li><b>none</b> - Place at hit position (replaces existing block)</li>
 *   <li><b>source_facing</b> - Place in direction source is facing (default)</li>
 *   <li><b>up</b> - Place above hit position</li>
 *   <li><b>down</b> - Place below hit position</li>
 *   <li><b>north/south/east/west</b> - Place in specific direction</li>
 * </ul>
 *
 * <h3>Performance Note:</h3>
 * Block placement is relatively inexpensive but modifies world state.
 * Avoid using on rapid-fire weapons without cooldowns.
 */
public record PlaceBlockEffect(
		Identifier block,
		OffsetDirection offsetDirection,
		boolean replaceAirOnly
) implements OnHitBlockEffect {
	public static final String TYPE = "forgero:place_block";

	public static final Codec<PlaceBlockEffect> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Identifier.CODEC.fieldOf("block").forGetter(PlaceBlockEffect::block),
			OffsetDirection.CODEC.optionalFieldOf("offset_direction", OffsetDirection.SOURCE_FACING).forGetter(PlaceBlockEffect::offsetDirection),
			Codec.BOOL.optionalFieldOf("replace_air_only", true).forGetter(PlaceBlockEffect::replaceAirOnly)
	).apply(instance, PlaceBlockEffect::new));

	@Override
	public void apply(World world, Entity source, BlockPos pos) {
		if (world.isClient()) {
			return;
		}

		// Calculate placement position based on offset direction
		BlockPos placePos = calculatePlacePosition(pos, source);

		// Check if we can place at this position
		if (!canPlaceAt(world, placePos)) {
			return;
		}

		// Get the block to place
		Block blockToPlace = Registries.BLOCK.get(block);
		if (blockToPlace == null || blockToPlace == Blocks.AIR) {
			return;
		}

		// Place the block
		BlockState stateToPlace = blockToPlace.getDefaultState();
		world.setBlockState(placePos, stateToPlace, 3);
	}

	private BlockPos calculatePlacePosition(BlockPos hitPos, Entity source) {
		return switch (offsetDirection) {
			case NONE -> hitPos;
			case SOURCE_FACING -> {
				Direction facing = source.getHorizontalFacing().getOpposite();
				yield hitPos.offset(facing);
			}
			case UP -> hitPos.up();
			case DOWN -> hitPos.down();
			case NORTH -> hitPos.north();
			case SOUTH -> hitPos.south();
			case EAST -> hitPos.east();
			case WEST -> hitPos.west();
		};
	}

	private boolean canPlaceAt(World world, BlockPos pos) {
		// Check if position is valid for block placement
		if (!world.canSetBlock(pos)) {
			return false;
		}

		// If replace_air_only is true, only place in air blocks
		if (replaceAirOnly) {
			return world.getBlockState(pos).isAir();
		}

		return true;
	}

	@Override
	public String type() {
		return TYPE;
	}

	/**
	 * Enum defining where to place the block relative to the hit position.
	 */
	public enum OffsetDirection {
		NONE("none"),
		SOURCE_FACING("source_facing"),
		UP("up"),
		DOWN("down"),
		NORTH("north"),
		SOUTH("south"),
		EAST("east"),
		WEST("west");

		private final String id;

		OffsetDirection(String id) {
			this.id = id;
		}

		public String getId() {
			return id;
		}

		public static final Codec<OffsetDirection> CODEC = Codec.STRING.xmap(
				id -> {
					for (OffsetDirection dir : values()) {
						if (dir.getId().equals(id)) {
							return dir;
						}
					}
					throw new IllegalArgumentException("Unknown offset direction: " + id + ". Valid values: none, source_facing, up, down, north, south, east, west");
				},
				OffsetDirection::getId
		);
	}
}
