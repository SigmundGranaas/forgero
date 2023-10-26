package com.sigmundgranaas.forgero.minecraft.common.handler.blockbreak.selector;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.core.property.v2.feature.HandlerBuilder;
import com.sigmundgranaas.forgero.core.property.v2.feature.JsonBuilder;
import com.sigmundgranaas.forgero.minecraft.common.feature.ModifiableFeatureAttribute;
import com.sigmundgranaas.forgero.minecraft.common.handler.blockbreak.filter.BlockFilter;
import org.jetbrains.annotations.NotNull;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

/**
 * A block selector that selects blocks based on a String pattern.
 * The pattern is a list of strings, where each string represents a row in the pattern.
 * Each character in the string represents a block in the pattern.
 * <p>
 * X values represent blocks that should be selected, and empty spaces should be ignored.
 * A C value represents the root position of the pattern. If a c is not present, the root position is assumed to be the center of the pattern.
 * <p>
 * The pattern is rotated based on the player's facing direction.
 * The pattern is applied horizontally or vertically based on the player's facing direction.
 */
public class PatternSelector implements BlockSelector {
	public static final String TYPE = "forgero:pattern";
	public static final JsonBuilder<PatternSelector> BUILDER = HandlerBuilder.fromObject(PatternSelector.class, PatternSelector::fromJson);
	public static String multiDirection = "multi";
	public static String horizontalDirection = "horizontal";
	public static String verticalDirection = "vertical";
	private final List<String> pattern;
	private final BlockFilter filter;
	private final ModifiableFeatureAttribute depth;
	private final String direction;


	public PatternSelector(List<String> pattern, BlockFilter filter, ModifiableFeatureAttribute depth, String direction) {
		this.pattern = pattern;
		this.filter = filter;
		this.depth = depth;
		this.direction = direction;
	}

	public static PatternSelector fromJson(JsonObject json) {
		Type typeOfList = new TypeToken<List<String>>() {
		}.getType();
		Gson gson = new Gson();
		List<String> pattern = json.has("pattern") ? gson.fromJson(json.get("pattern").getAsJsonArray(), typeOfList) : List.of("");
		BlockFilter filter = BlockFilter.fromJson(json.get("filter"));
		ModifiableFeatureAttribute depth = ModifiableFeatureAttribute.of(json, "depth", "forgero:pattern_mining_depth");
		String direction = json.has("direction") ? json.get("direction").getAsString() : "multi"; // default direction

		return new PatternSelector(pattern, filter, depth, direction);
	}

	public Set<BlockPos> selectWithDepth(BlockPos rootPos, Entity source) {
		Set<BlockPos> blocks = new HashSet<>();
		Direction depthDirection = determineDepthDirection(source);
		int depthValue = this.depth.with(source).asInt();

		for (int d = 0; d < depthValue; d++) {
			BlockPos offsetPos = rootPos.offset(depthDirection, d);
			blocks.addAll(selectPattern(offsetPos, source));
		}
		return blocks;
	}

	private Direction determineDepthDirection(Entity source) {
		Direction primaryFacing = Direction.getEntityFacingOrder(source)[0];
		Direction secondaryFacing = Direction.getEntityFacingOrder(source)[1];
		boolean primaryUpOrDown = primaryFacing == Direction.UP || primaryFacing == Direction.DOWN;
		if (direction.equals(multiDirection)) {
			return (primaryFacing != Direction.DOWN && primaryFacing != Direction.UP) ? primaryFacing : Direction.UP;
		} else {
			if (direction.equals(horizontalDirection)) {
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
	@Override
	public Set<BlockPos> select(BlockPos rootPos, Entity source) {
		return selectWithDepth(rootPos, source);
	}

	@NotNull
	public Set<BlockPos> selectPattern(BlockPos rootPos, Entity source) {
		Direction facingHorizontal = source.getHorizontalFacing();
		Direction[] primaryFacing = Direction.getEntityFacingOrder(source);
		Set<BlockPos> blocks = new HashSet<>();
		//iterate through the pattern list, and find all the blocks that match the pattern

		boolean vertical;
		//determine if the pattern should be applied horizontally or vertically based on player facing direction and direction variable
		if (direction.equals(horizontalDirection)) {
			vertical = false;
		} else if (direction.equals(verticalDirection)) {
			vertical = true;
		} else {
			vertical = primaryFacing[0] != Direction.DOWN && primaryFacing[0] != Direction.UP;
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

					//Flatten the pattern player if facing up or down
					if (!vertical) {
						pos = rotate(pos, 1, Direction.Axis.X);
					}
					//Rotate the block based on the player's facing direction
					pos = rotate(pos, rotationAmount(facingHorizontal), Direction.Axis.Y);

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
		switch (axis) {
			case X -> {
				return new BlockPos(x, z, -y);
			}
			case Y -> {
				return new BlockPos(-z, y, x);
			}
			case Z -> {
				return new BlockPos(y, -x, z);
			}
		}
		return pos;
	}

	private boolean isValidEntry(char c) {
		return c == 'x' || c == 'X' || c == 'c' || c == 'C' || c == ' ';
	}
}
