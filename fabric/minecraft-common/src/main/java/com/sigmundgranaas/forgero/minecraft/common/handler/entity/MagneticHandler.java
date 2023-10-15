package com.sigmundgranaas.forgero.minecraft.common.handler.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.property.v2.Attribute;
import com.sigmundgranaas.forgero.core.property.v2.feature.HandlerBuilder;
import com.sigmundgranaas.forgero.core.property.v2.feature.JsonBuilder;
import com.sigmundgranaas.forgero.minecraft.common.handler.targeted.onHitBlock.OnHitBlockHandler;
import com.sigmundgranaas.forgero.minecraft.common.handler.targeted.onHitEntity.OnHitHandler;
import com.sigmundgranaas.forgero.minecraft.common.service.StateService;
import lombok.Getter;
import lombok.experimental.Accessors;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

/**
 * Represents a handler that either pulls or pushes entities based on configuration.
 * The handler can be configured using a JSON format.
 *
 * <p>
 * Example JSON configurations:
 * </p>
 *
 * <pre>
 * Pulling entities towards the source:
 * {
 *   "type": "forgero:magnetic",
 *   "power": 1.5,
 *   "distance": 5,
 *   "filter": "minecraft:item",
 *   "pushAway": false
 * }
 *
 * Pushing entities away from the source:
 * {
 *   "type": "forgero:magnetic",
 *   "power": 2.0,
 *   "distance": 6,
 *   "filter": "minecraft:zombie",
 *   "pushAway": true
 * }
 *
 * Pulling multiple types of entities:
 * {
 *   "type": "forgero:magnetic",
 *   "power": 1.8,
 *   "distance": 7,
 *   "filter": ["minecraft:item", "minecraft:skeleton", "minecraft:creeper"],
 *   "pushAway": false
 * }
 * </pre>
 *
 * <p>
 * Note: If the "pushAway" field is absent, the default behavior is to pull entities.
 * </p>
 */
@Getter
@Accessors(fluent = true)
public class MagneticHandler implements EntityHandler, OnHitBlockHandler, OnHitHandler {
	public static final String TYPE = "forgero:magnetic";
	public static final String MAGNETIC_POWER_ATTRIBUTE_TYPE = "forgero:magnetic_power";
	public static final String MAGNETIC_RANGE_ATTRIBUTE_TYPE = "forgero:magnetic_range";
	public static final JsonBuilder<MagneticHandler> BUILDER = HandlerBuilder.fromObject(MagneticHandler.class, MagneticHandler::fromJson);

	private final float power;
	private final int distance;
	private final boolean pushAway; // True if pushing entities away, false if pulling them
	private final List<EntityType<?>> entityFilters;

	/**
	 * Constructs a new {@link MagneticHandler} with the specified properties.
	 *
	 * @param power    The strength of the magnetic pull effect.
	 * @param distance The range to search for items to pull.
	 */
	public MagneticHandler(float power, int distance, List<EntityType<?>> entityFilters, boolean pushAway) {
		this.power = power;
		this.distance = distance;
		this.entityFilters = entityFilters;
		this.pushAway = pushAway;
	}

	/**
	 * Constructs a {@link MagneticHandler} from a JSON object.
	 *
	 * @param json The JSON object.
	 * @return A new instance of {@link MagneticHandler}.
	 */
	public static MagneticHandler fromJson(JsonObject json) {
		float power = json.get("power").getAsFloat();
		int distance = json.get("distance").getAsInt();
		boolean pushAway = false;

		if (json.has("pushAway")) {
			pushAway = json.get("pushAway").getAsBoolean();
		}

		List<EntityType<?>> entityFilters = new ArrayList<>();
		if (json.has("filter")) {
			JsonElement filterElement = json.get("filter");
			if (filterElement.isJsonArray()) {
				for (JsonElement filter : filterElement.getAsJsonArray()) {
					addEntityTypeFromIdentifier(entityFilters, filter.getAsString());
				}
			} else if (filterElement.isJsonPrimitive()) {
				addEntityTypeFromIdentifier(entityFilters, filterElement.getAsString());
			}
		}

		return new MagneticHandler(power, distance, entityFilters, pushAway);
	}

	private static void addEntityTypeFromIdentifier(List<EntityType<?>> entityFilters, String identifier) {
		EntityType<?> type = Registry.ENTITY_TYPE.get(Identifier.tryParse(identifier));
		entityFilters.add(type);
	}

	public static Optional<? extends PropertyContainer> propertyHelper(Entity entity) {
		if (entity instanceof LivingEntity living) {
			return StateService.INSTANCE.convert(living.getMainHandStack());
		}
		return Optional.empty();
	}

	@Override
	public void handle(Entity rootEntity) {
		Vec3d rootVec = rootEntity.getPos();

		float calculatedRange = propertyHelper(rootEntity)
				.map(container -> Attribute.of(container, MAGNETIC_RANGE_ATTRIBUTE_TYPE))
				.map(Attribute::asFloat)
				.orElse(0f) + distance;
		List<Entity> nearbyEntities = getNearbyEntities(rootEntity, calculatedRange, entity -> entity instanceof ItemEntity);
		pullEntities(rootVec, nearbyEntities, rootEntity);
	}

	private List<Entity> getNearbyEntities(Entity rootEntity, float range, Predicate<Entity> predicate) {
		Vec3d rootVec = rootEntity.getPos();
		BlockPos pos1 = new BlockPos(rootVec.x + range, rootVec.y + range, rootVec.z + range);
		BlockPos pos2 = new BlockPos(rootVec.x - range, rootVec.y - range, rootVec.z - range);
		return rootEntity.getWorld().getOtherEntities(rootEntity, new Box(pos1, pos2), predicate);
	}

	public void pullEntities(Vec3d rootVec, List<Entity> entities, Entity rootEntity) {
		for (Entity nearbyEntity : entities) {
			double dist = nearbyEntity.getPos().distanceTo(rootVec);

			float calculatedPower = propertyHelper(rootEntity)
					.map(container -> Attribute.of(container, MAGNETIC_POWER_ATTRIBUTE_TYPE))
					.map(Attribute::asFloat)
					.orElse(0f) + power;

			Vec3d velocity = nearbyEntity.getPos()
					.relativize(rootVec)
					.normalize()
					.multiply(0.02f * calculatedPower);

			if (pushAway) {
				velocity = velocity.multiply(-1); // Reverse the direction
			}

			if (dist < 1) {
				nearbyEntity.addVelocity(0, 0, 0);
			} else {
				nearbyEntity.addVelocity(velocity.x, velocity.y, velocity.z);
			}
		}
	}

	@Override
	public void onHit(Entity root, World world, BlockPos pos) {
		handle(root);
	}

	@Override
	public void onHit(Entity root, World world, Entity target) {
		handle(root);
	}
}
