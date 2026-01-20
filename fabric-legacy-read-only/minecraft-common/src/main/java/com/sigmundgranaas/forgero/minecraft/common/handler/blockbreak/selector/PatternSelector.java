package com.sigmundgranaas.forgero.minecraft.common.handler.blockbreak.selector;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.core.property.v2.feature.HandlerBuilder;
import com.sigmundgranaas.forgero.core.property.v2.feature.JsonBuilder;
import com.sigmundgranaas.forgero.minecraft.common.feature.ModifiableFeatureAttribute;
import com.sigmundgranaas.forgero.minecraft.common.handler.blockbreak.filter.BlockFilter;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/**
 * <h1>PatternSelector</h1>
 *
 * <p>The PatternSelector class is responsible for selecting blocks based on a pattern defined in strings.
 * Each character in these strings represents a different instruction for block selection.</p>
 *
 * <h2>JSON Configuration</h2>
 *
 * <p>The PatternSelector uses a JSON object for configuration. The JSON object can contain the following keys:</p>
 *
 * <ul>
 *   <li><b>pattern</b>: An array of strings that define the pattern. Each string represents a row in the pattern.</li>
 *   <li><b>depth</b>: (Optional) An integer specifying how deep the pattern should be applied relative to the root position. Defaults to 1</li>
 *   <li><b>direction</b>: (Optional) A string specifying how the pattern should be applied ("horizontal", "vertical", or "multi"). Default to "multi".</li>
 *   <li><b>filter</b>: (Optional) The filter for further block selection. Can be a string, and array or an object.</li>
 * </ul>
 *
 * <h3>Modifiers</h3>
 *
 * <p>The PatternSelector recognizes a special string value as a modifier:</p>
 *
 * <ul>
 *   <li><b>"forgero:pattern_mining_depth"</b>: This string value acts as a key to dynamically modify the mining depth during based on attributes from the source entity</li>
 * </ul>
 *
 * <h3>Pattern Characters:</h3>
 *
 * <p>The pattern strings can contain the following characters:</p>
 *
 * <ul>
 *   <li><b>"X"</b>: Specifies a block to be selected.</li>
 *   <li><b>"C"</b>: Specifies the root position of the pattern. If not present, the center is considered the root.</li>
 *   <li><b>" "</b> (Space): Specifies blocks that should be ignored.</li>
 * </ul>
 *
 * <h4>3x3 Mining example:</h4>
 *
 * <pre>
 * {
 *   "type": "minecraft:block_breaking",
 *   "selector": {
 *     "type": "forgero:pattern",
 *     "pattern": ["xxx", "xcx", "xxx"],
 *     "depth": 2,
 *     "direction": "horizontal",
 *     "filter": "forgero:can_mine"
 *   }
 * }
 * </pre>
 */
public class PatternSelector implements BlockSelector {
	public static final String TYPE = "forgero:pattern";
	public static final String DEPTH_MODIFIER = "forgero:pattern_mining_depth";
	public static final String DEPTH_MODIFIER_KEY = "depth";

	public static final ModifiableFeatureAttribute.Builder MODIFIER_BUILDER = ModifiableFeatureAttribute
			.builder(DEPTH_MODIFIER)
			.key(DEPTH_MODIFIER_KEY)
			.defaultValue(1);

	public static final String PATTERN_KEY = "pattern";

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
		List<String> pattern = parsePattern(json);
		BlockFilter filter = HandlerBuilder.DEFAULT.build(BlockFilter.KEY, json.get("filter")).orElseThrow();
		ModifiableFeatureAttribute depth = MODIFIER_BUILDER.build(json);
		String direction = json.has("direction") ? json.get("direction").getAsString() : "multi"; // default direction
		return new PatternSelector(pattern, filter, depth, direction);
	}

	public static List<String> parsePattern(JsonObject object) {
		List<String> fallback = Collections.emptyList();
		Supplier<String> example = () -> """
				"pattern": [
				"xxx",
				"xxx",
				"xxx"
				]
								
				""";

		Supplier<String> fallbackMessage = () -> "Setting an empty fallback pattern, which will not select any blocks.";
		if (!object.has(PATTERN_KEY)) {
			Forgero.LOGGER.warn("Missing pattern key for object pattern selection object. This is a required field. Here is an example of how the field can look: \n {} \n Here is the actual object: \n {}", example.get(), object);
			Forgero.LOGGER.warn(fallbackMessage.get());
			return fallback;
		}
		if (!object.get(PATTERN_KEY).isJsonArray()) {
			Forgero.LOGGER.warn("Encountered unknown pattern data: \n {}. Only lists are supported. Here is an example of a correct pattern: \n {} \n Here is the whole object: \n", object.get(PATTERN_KEY), example.get());
			Forgero.LOGGER.warn(fallbackMessage.get());
			return fallback;
		}
		return stringListFromJson(object.getAsJsonArray(PATTERN_KEY));
	}

	public static List<String> stringListFromJson(JsonArray array) {
		Type typeOfList = new TypeToken<List<String>>() {
		}.getType();
		Gson gson = new Gson();
		return gson.fromJson(array, typeOfList);
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

	private boolean isAir(BlockPos rootPos, Entity source) {
		return source.getWorld().getBlockState(rootPos).isAir();
	}

	@NotNull
	public Set<BlockPos> selectPattern(BlockPos rootPos, Entity source) {
		Direction facing = source.getHorizontalFacing();
		Direction[] primaryFacing = Direction.getEntityFacingOrder(source);
		Set<BlockPos> blocks = new HashSet<>();
		//iterate through the pattern list, and find all the blocks that match the pattern


		for (Direction direction : primaryFacing) {
			if (isAir(rootPos.offset(direction.getOpposite()), source)) {
				facing = direction;
				break;
			}
		}

		boolean vertical;
		//determine if the pattern should be applied horizontally or vertically based on player facing direction and direction variable
		if (direction.equals(horizontalDirection)) {
			vertical = false;
		} else if (direction.equals(verticalDirection)) {
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
		return c == 'x' || c == 'X' || c == 'c' || c == 'C';
	}
}
