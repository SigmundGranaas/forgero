package com.sigmundgranaas.forgero.properties.minecraft.blockbreaking.selector;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.properties.minecraft.blockbreaking.filter.BlockFilter;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public record PatternSelector(
		List<String> pattern,
		int depth,
		String direction,
		BlockFilter filter
) implements BlockSelector {
	public static final String TYPE = "forgero:pattern";

	public static final Codec<PatternSelector> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.listOf().fieldOf("pattern").forGetter(PatternSelector::pattern),
			Codec.INT.optionalFieldOf("depth", 1).forGetter(PatternSelector::depth),
			Codec.STRING.optionalFieldOf("direction", "multi").forGetter(PatternSelector::direction),
			BlockFilter.CODEC.fieldOf("filter").forGetter(PatternSelector::filter)
	).apply(instance, PatternSelector::new));

	@Override
	public String type() {
		return TYPE;
	}

	@NotNull
	@Override
	public Set<BlockPos> select(BlockPos rootPos, Entity source) {
		Set<BlockPos> blocks = new HashSet<>();
		Direction depthDirection = determineDepthDirection(source);

		for (int d = 0; d < depth; d++) {
			BlockPos offsetPos = rootPos.offset(depthDirection, d);
			blocks.addAll(selectPattern(offsetPos, source));
		}
		return blocks;
	}

	private Direction determineDepthDirection(Entity source) {
		Direction primaryFacing = Direction.getEntityFacingOrder(source)[0];
		Direction secondaryFacing = Direction.getEntityFacingOrder(source)[1];
		boolean primaryUpOrDown = primaryFacing == Direction.UP || primaryFacing == Direction.DOWN;
		if (direction.equals("multi")) {
			return (primaryFacing != Direction.DOWN && primaryFacing != Direction.UP) ? primaryFacing : Direction.UP;
		} else {
			if (direction.equals("horizontal")) {
				if (primaryUpOrDown) {
					return primaryFacing;
				} else {
					if (secondaryFacing == Direction.UP || secondaryFacing == Direction.DOWN) {
						return secondaryFacing;
					} else {
						return Direction.DOWN;
					}
				}
			} else {
				return primaryUpOrDown ? secondaryFacing : primaryFacing;
			}
		}
	}

	@NotNull
	public Set<BlockPos> selectPattern(BlockPos rootPos, Entity source) {
		Direction facing = source.getHorizontalFacing();
		Direction[] primaryFacing = Direction.getEntityFacingOrder(source);
		Set<BlockPos> blocks = new HashSet<>();
		//iterate through the pattern list, and find all the blocks that match the pattern
		for (Direction direction : primaryFacing) {
			if (source.getWorld().getBlockState(rootPos.offset(direction.getOpposite())).isAir()) {
				facing = direction;
				break;
			}
		}

		boolean vertical;
		//determine if the pattern should be applied horizontally or vertically based on player facing direction and direction variable
		if (direction.equals("horizontal")) {
			vertical = false;
		} else if (direction.equals("vertical")) {
			vertical = true;
		} else {
			vertical = facing != Direction.DOWN && facing != Direction.UP;
		}

		//iterate through the pattern and check if the blocks match the pattern
		for (int i = 0; i < pattern.size(); i++) {
			for (int j = 0; j < pattern.get(i).length(); j++) {
				var slice = pattern.get(i);
				//If the pattern matches, add the block to the list of blocks that should be broken
				if (isValidEntry(slice.charAt(j))) {

					//determine the position of the block relative to the root position
					int x = j;
					int y = i;
					int z = 0;

					BlockPos pos = new BlockPos(x, y, z);
					//Center the pattern
					pos = centerOffset().subtract(pos);

					//Flatten the pattern if player is facing up or down
					if (!vertical) {
						pos = rotate(pos, 1, Direction.Axis.X);
					}
					//Rotate the block based on the player's facing direction
					pos = rotate(pos, rotationAmount(facing), Direction.Axis.Y);

					//Apply absolute position
					pos = rootPos.add(pos);
					if (filter.filter(source, pos, rootPos)) {
						blocks.add(pos);
					}
				}
			}
		}
		return blocks;
	}

	private BlockPos centerOffset() {
		int height = pattern.size();
		int width = pattern.get(0).length();

		int x = width / 2;
		int y = height == 2 ? 0 : height / 2;
		int z = 0;
		return new BlockPos(x, y, z);
	}

	private BlockPos rotate(BlockPos pos, int times, Direction.Axis axis) {
		for (int i = 0; i < times; i++) {
			pos = applyRotation(pos, axis);
		}
		return pos;
	}

	private int rotationAmount(Direction direction) {
		int rotation;
		rotation = switch (direction) {
			case EAST -> 1;
			case SOUTH -> 2;
			case WEST -> 3;
			default -> 0;
		};
		return rotation;
	}

	private BlockPos applyRotation(BlockPos pos, Direction.Axis axis) {
		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();
		return switch (axis) {
			case X -> new BlockPos(x, z, -y);
			case Y -> new BlockPos(-z, y, x);
			case Z -> new BlockPos(y, -x, z);
		};
	}

	private boolean isValidEntry(char c) {
		return c == 'x' || c == 'X' || c == 'c' || c == 'C';
	}
}
