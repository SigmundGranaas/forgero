package com.sigmundgranaas.forgero.minecraft.common.handler.targeted.onHitBlock;

import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.core.property.v2.feature.HandlerBuilder;
import com.sigmundgranaas.forgero.core.property.v2.feature.JsonBuilder;
import lombok.Getter;
import lombok.experimental.Accessors;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

/**
 * Represents a handler that places a specified block with a given block state at the targeted block position upon being hit.
 *
 * <p>Example JSON configuration:
 * <pre>
 * {
 *   "type": "minecraft:on_hit_block",
 *   "on_hit": {
 *     "type": "minecraft:place_block",
 *     "block": "minecraft:torch",
 *     "state": {
 *         "facing": "north"
 *     }
 *   }
 * }
 * </pre>
 * </p>
 */
@Getter
@Accessors(fluent = true)
public class PlaceBlockHandler implements OnHitBlockHandler {

	public static final String TYPE = "minecraft:place_block";
	public static final JsonBuilder<OnHitBlockHandler> BUILDER = HandlerBuilder.fromObject(KEY.clazz(), PlaceBlockHandler::fromJson);

	private final Block block;
	private final BlockState state;

	/**
	 * Constructs a new {@link PlaceBlockHandler} with the specified block and block state.
	 *
	 * @param block The block to be placed.
	 * @param state The block state to be applied.
	 */
	public PlaceBlockHandler(Block block, BlockState state) {
		this.block = block;
		this.state = state;
	}

	/**
	 * Constructs an {@link PlaceBlockHandler} from a JSON object.
	 *
	 * @param json The JSON object.
	 * @return A new instance of {@link PlaceBlockHandler}.
	 */
	public static PlaceBlockHandler fromJson(JsonObject json) {
		Block block = Registry.BLOCK.get(new Identifier(json.get("block").getAsString()));

		BlockState state = block.getDefaultState();
		if (json.has("state")) {
			JsonObject stateObject = json.getAsJsonObject("state");
			state = applyStateFromJson(block, stateObject);
		}

		return new PlaceBlockHandler(block, state);
	}

	private static BlockState applyStateFromJson(Block block, JsonObject stateObject) {
		BlockState state = block.getDefaultState();

		for (Map.Entry<String, JsonElement> entry : stateObject.entrySet()) {
			String propertyName = entry.getKey();
			Property<?> property = block.getStateManager().getProperty(propertyName);

			if (property instanceof DirectionProperty) {
				Direction direction = Direction.byName(entry.getValue().getAsString());
				state = state.with((DirectionProperty) property, direction);
			} else if (property instanceof BooleanProperty) {
				boolean boolValue = entry.getValue().getAsBoolean();
				state = state.with((BooleanProperty) property, boolValue);
			} else if (property instanceof IntProperty) {
				int intValue = entry.getValue().getAsInt();
				state = state.with((IntProperty) property, intValue);
			} else if (property instanceof EnumProperty) {
				String enumValue = entry.getValue().getAsString();
				state = setEnumProperty(state, (EnumProperty<?>) property, enumValue);
			}
		}
		return state;
	}

	private static <T extends Enum<T> & StringIdentifiable> BlockState setEnumProperty(BlockState state, EnumProperty<T> property, String value) {
		return state.with(property, property.getValues().stream()
				.filter(e -> e.asString().equalsIgnoreCase(value))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException("Invalid value for EnumProperty")));
	}

	/**
	 * This method is triggered upon hitting a block.
	 * It checks the direction the source entity is facing and places the block with the given block state.
	 *
	 * @param source The source entity.
	 * @param world  The world where the event occurred.
	 * @param pos    The block position being hit.
	 */
	@Override
	public void onHit(Entity source, World world, BlockPos pos) {
		if (!world.isClient) {
			Direction facingDirection = source.getHorizontalFacing().getOpposite(); // Getting the direction entity is facing
			BlockPos placePos = pos.offset(facingDirection); // Position to place the block

			if (world.canSetBlock(placePos)) {
				world.setBlockState(placePos, state, 3);
			}
		}
	}
}
