package com.sigmundgranaas.forgero.minecraft.common.handler.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.core.property.v2.feature.HandlerBuilder;
import com.sigmundgranaas.forgero.core.property.v2.feature.JsonBuilder;
import com.sigmundgranaas.forgero.minecraft.common.handler.targeted.onHitBlock.BlockTargetHandler;
import com.sigmundgranaas.forgero.minecraft.common.handler.targeted.onHitEntity.EntityTargetHandler;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Represents a handler that summons specified creatures near the targeted entity upon being hit.
 * The type and number of creatures, as well as their relative position, can be configured.
 *
 * <p>Example JSON configuration:
 * <pre>
 * {
 *   "type": "minecraft:on_hit",
 *   "on_hit": {
 *     "type": "minecraft:summon",
 *     "creatures": [
 *       {"type": "minecraft:zombie", "count": 2},
 *       {"type": "minecraft:skeleton", "count": 1}
 *     ],
 *     "target": "minecraft:targeted_entity"
 *   }
 * }
 * </pre>
 * </p>
 */
@Getter
@Accessors(fluent = true)
public class SummonHandler implements EntityTargetHandler, EntityBasedHandler, BlockTargetHandler {

	public static final String TYPE = "minecraft:summon";
	public static final JsonBuilder<SummonHandler> BUILDER = HandlerBuilder.fromObject(SummonHandler.class, SummonHandler::fromJson);

	private final String target;
	private final List<CreatureEntry> creatures;

	/**
	 * Constructs a new {@link SummonHandler} with the specified target and creatures list.
	 *
	 * @param target    The target entity.
	 * @param creatures List of creatures to be summoned with their counts.
	 */
	public SummonHandler(String target, List<CreatureEntry> creatures) {
		this.target = target;
		this.creatures = creatures;
	}

	/**
	 * Constructs a {@link SummonHandler} from a JSON object.
	 *
	 * @param json The JSON object.
	 * @return A new instance of {@link SummonHandler}.
	 */
	public static SummonHandler fromJson(JsonObject json) {
		String target = json.get("target").getAsString();
		List<CreatureEntry> creatures = new ArrayList<>();

		JsonArray creaturesArray = json.getAsJsonArray("creatures");
		for (JsonElement element : creaturesArray) {
			JsonObject creatureObj = element.getAsJsonObject();
			String type = creatureObj.get("type").getAsString();
			int count = creatureObj.get("count").getAsInt();
			creatures.add(new CreatureEntry(type, count));
		}

		return new SummonHandler(target, creatures);
	}

	/**
	 * This method is triggered upon hitting an entity.
	 * Summons the specified creatures near the targeted entity.
	 *
	 * @param source       The source entity.
	 * @param world        The world where the event occurred.
	 * @param targetEntity The targeted entity.
	 */
	@Override
	public void onHit(Entity source, World world, Entity targetEntity) {
		if (world instanceof ServerWorld serverWorld && creatures != null) {
			if ("minecraft:targeted_entity".equals(target)) {
				execute(serverWorld, new Vec3d(targetEntity.getX() + (world.random.nextDouble() - 0.5) * 2, targetEntity.getY(), targetEntity.getZ() + (world.random.nextDouble() - 0.5) * 2));
			} else {
				execute(serverWorld, new Vec3d(source.getX() + (world.random.nextDouble() - 0.5) * 2, source.getY(), source.getZ() + (world.random.nextDouble() - 0.5) * 2));
			}
		}
	}

	public void execute(ServerWorld world, Vec3d pos) {
		for (CreatureEntry entry : creatures) {
			for (int i = 0; i < entry.count(); i++) {
				Optional<EntityType<?>> entityOpt = EntityType.get(entry.type());
				if (entityOpt.isPresent()) {
					Entity newEntity = entityOpt.get().create(world);
					if (newEntity != null) {
						newEntity.refreshPositionAndAngles(pos.getX(), pos.getY(), pos.getZ(), world.random.nextFloat() * 360.0F, 0.0F);
						world.spawnEntity(newEntity);
					}
				}
			}
		}
	}

	@Override
	public void handle(Entity entity) {
		onHit(entity, entity.getWorld(), entity);
	}

	@Override
	public void onHit(Entity root, World world, BlockPos pos) {
		if (world instanceof ServerWorld serverWorld && creatures != null) {
			if ("minecraft:targeted_entity".equals(target)) {
				execute(serverWorld, new Vec3d(pos.getX() + (world.random.nextDouble() - 0.5) * 2, pos.getY(), pos.getZ() + (world.random.nextDouble() - 0.5) * 2));
			} else {
				execute(serverWorld, new Vec3d(root.getX() + (world.random.nextDouble() - 0.5) * 2, root.getY(), root.getZ() + (world.random.nextDouble() - 0.5) * 2));
			}
		}
	}

	@Data
	@AllArgsConstructor
	public static class CreatureEntry {
		private final String type;
		private final int count;
	}
}
