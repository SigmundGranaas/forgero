package com.sigmundgranaas.forgero.effects.zone;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import com.sigmundgranaas.forgero.effects.entity.ContextualEffectHandler;
import com.sigmundgranaas.forgero.effects.entity.EntityEffectHandler;
import com.sigmundgranaas.forgero.effects.entity.OnHitEffect;
import com.sigmundgranaas.forgero.properties.minecraft.entityfilter.EntityFilter;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;

/**
 * Tracks active persistent zones and re-applies their effects to entities inside on an interval,
 * until they expire. Ticked once per server world via {@link ServerTickEvents#END_WORLD_TICK};
 * the listener is installed lazily the first time a zone is created.
 *
 * <p>State is in-memory: zones in flight do not survive a server restart, which is acceptable for
 * the seconds-scale fields this supports.
 */
public final class ZoneManager {
	private static final CopyOnWriteArrayList<Zone> ZONES = new CopyOnWriteArrayList<>();
	private static volatile boolean initialized = false;

	private ZoneManager() {
	}

	public static synchronized void register(Zone zone) {
		ensureTicker();
		ZONES.add(zone);
	}

	public static void clearAll() {
		ZONES.clear();
	}

	public static int activeZoneCount() {
		return ZONES.size();
	}

	private static synchronized void ensureTicker() {
		if (initialized) {
			return;
		}
		ServerTickEvents.END_WORLD_TICK.register(ZoneManager::tickWorld);
		initialized = true;
	}

	private static void tickWorld(ServerWorld world) {
		if (ZONES.isEmpty()) {
			return;
		}
		long now = world.getTime();
		for (Zone zone : ZONES) {
			if (!zone.worldKey().equals(world.getRegistryKey().getValue().toString())) {
				continue;
			}
			if (now >= zone.expiry()) {
				ZONES.remove(zone);
				continue;
			}
			if ((now % zone.interval()) == 0) {
				zone.apply(world);
			}
		}
	}

	/**
	 * A single active zone. {@code worldKey} is the world's registry-id string so we can match it in
	 * the per-world tick without holding a hard world reference.
	 */
	public record Zone(
			String worldKey,
			Vec3d center,
			double radius,
			long expiry,
			int interval,
			List<OnHitEffect> effects,
			List<EntityFilter> filters,
			@Nullable UUID ownerId,
			OwnerMode ownerMode
	) {
		/**
		 * The living entities currently inside the zone that pass its filters. Exposed for testing
		 * the spatial/filter selection independently of effect application.
		 */
		public List<Entity> selectTargets(ServerWorld world) {
			Entity owner = ownerId != null ? world.getEntity(ownerId) : null;
			double radiusSquared = radius * radius;
			Box box = new Box(center.x - radius, center.y - radius, center.z - radius,
					center.x + radius, center.y + radius, center.z + radius);
			List<Entity> candidates = world.getOtherEntities(owner, box,
					e -> e instanceof LivingEntity && e.squaredDistanceTo(center) <= radiusSquared);
			return candidates.stream()
					.filter(target -> filters.stream().allMatch(f -> f.test(owner != null ? owner : target, target)))
					.toList();
		}

		void apply(ServerWorld world) {
			Entity owner = ownerId != null ? world.getEntity(ownerId) : null;
			if (ownerMode == OwnerMode.OWNER_REQUIRED && owner == null) {
				return;
			}

			for (Entity target : selectTargets(world)) {
				Entity effectSource = ownerMode == OwnerMode.TARGET_AS_SOURCE ? target : owner;
				for (OnHitEffect effect : effects) {
					if (effect instanceof ContextualEffectHandler contextual) {
						if (effectSource != null) {
							contextual.apply(effectSource, target);
						}
						// keep_owner with an absent owner: skip source-dependent effects.
					} else if (effect instanceof EntityEffectHandler simple) {
						simple.apply(target);
					}
				}
			}
		}
	}
}
