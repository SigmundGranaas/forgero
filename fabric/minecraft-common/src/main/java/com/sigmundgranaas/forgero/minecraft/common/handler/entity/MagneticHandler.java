package com.sigmundgranaas.forgero.minecraft.common.handler.entity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.core.property.Attribute;
import com.sigmundgranaas.forgero.core.property.attribute.BaseAttribute;
import com.sigmundgranaas.forgero.core.property.v2.feature.HandlerBuilder;
import com.sigmundgranaas.forgero.core.property.v2.feature.JsonBuilder;
import com.sigmundgranaas.forgero.minecraft.common.handler.targeted.onHitBlock.BlockTargetHandler;
import com.sigmundgranaas.forgero.minecraft.common.handler.targeted.onHitEntity.EntityTargetHandler;
import lombok.Getter;
import lombok.experimental.Accessors;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
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
 *   "range": 5,
 *   "filter": "minecraft:item",
 *   "pushAway": false
 * }
 *
 * Pushing entities away from the source:
 * {
 *   "type": "forgero:magnetic",
 *   "power": 2.0,
 *   "range": 6,
 *   "filter": "minecraft:zombie",
 *   "pushAway": true
 * }
 *
 * Pulling multiple types of entities:
 * {
 *   "type": "forgero:magnetic",
 *   "power": 1.8,
 *   "range": 7,
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
public class MagneticHandler implements EntityBasedHandler, BlockTargetHandler, EntityTargetHandler {
	public static final String TYPE = "forgero:magnetic";
	public static final String MAGNETIC_POWER_ATTRIBUTE_TYPE = "forgero:magnetic_power";
	public static final String MAGNETIC_RANGE_ATTRIBUTE_TYPE = "forgero:magnetic_range";
	public static final JsonBuilder<MagneticHandler> BUILDER = HandlerBuilder.fromObject(MagneticHandler.class, MagneticHandler::fromJson);

	private final Attribute power;
	private final Attribute distance;
	private final boolean pushAway; // True if pushing entities away, false if pulling them
	private final Set<EntityType<?>> entityFilters;

	/**
	 * Constructs a new {@link MagneticHandler} with the specified properties.
	 *
	 * @param power    The strength of the magnetic pull effect.
	 * @param distance The range to search for items to pull.
	 */
	public MagneticHandler(Attribute power, Attribute distance, Set<EntityType<?>> entityFilters, boolean pushAway) {
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
		float power = json.has("power") ? json.get("power").getAsFloat() : 0;
		float distance = json.has("range") ? json.get("range").getAsFloat() : 0;
		boolean pushAway = false;

		if (json.has("pushAway")) {
			pushAway = json.get("pushAway").getAsBoolean();
		}

		Set<EntityType<?>> entityFilters = new HashSet<>();
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

		return new MagneticHandler(BaseAttribute.of(power, MAGNETIC_POWER_ATTRIBUTE_TYPE), BaseAttribute.of(distance, MAGNETIC_RANGE_ATTRIBUTE_TYPE), entityFilters, pushAway);
	}

	private static void addEntityTypeFromIdentifier(Set<EntityType<?>> entityFilters, String identifier) {
		EntityType<?> type = Registries.ENTITY_TYPE.get(Identifier.tryParse(identifier));
		entityFilters.add(type);
	}

	float range(Entity source) {
		return compute(distance, source).asFloat();
	}

	float power(Entity source) {
		return compute(power, source).asFloat();
	}

	@Override
	public void handle(Entity rootEntity) {
		Vec3d rootVec = rootEntity.getPos();

		float range = range(rootEntity);

		List<Entity> nearbyEntities = getNearbyEntities(rootEntity, range, entity -> entityFilters.contains(entity.getType()));
		pullEntities(rootVec, nearbyEntities, rootEntity);
	}

	private List<Entity> getNearbyEntities(Entity rootEntity, float range, Predicate<Entity> predicate) {
		Vec3d rootVec = rootEntity.getPos();
		BlockPos pos1 = new BlockPos((int) (rootVec.x + range), (int) (rootVec.y + range), (int) (rootVec.z + range));
		BlockPos pos2 = new BlockPos((int) (rootVec.x - range), (int) (rootVec.y - range), (int) (rootVec.z - range));
		return rootEntity.getWorld().getOtherEntities(rootEntity, new Box(new Vec3d(pos1.getX(), pos1.getY(), pos1.getZ()), new Vec3d(pos2.getX(), pos2.getY(), pos2.getZ())), predicate);
	}

	public void pullEntities(Vec3d rootVec, List<Entity> entities, Entity rootEntity) {
		float computedPower = power(rootEntity);

		for (Entity nearbyEntity : entities) {
			double dist = nearbyEntity.getPos().distanceTo(rootVec);

			Vec3d velocity = nearbyEntity.getPos()
					.relativize(rootVec)
					.normalize()
					.multiply(0.02f * computedPower);

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
