package com.sigmundgranaas.forgero.effects.zone;

import java.util.Collections;
import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.effects.entity.ContextualEffectHandler;
import com.sigmundgranaas.forgero.effects.entity.OnHitEffect;
import com.sigmundgranaas.forgero.properties.minecraft.entityfilter.EntityFilter;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;

/**
 * Spawns a persistent area-of-effect zone centered on the target's position. The zone re-applies
 * its effect list to entities inside its radius every {@code interval} ticks until {@code duration}
 * elapses. Unlocks lingering fields (fire, healing totems, frost ground, etc.).
 */
public record CreateZoneHandler(
		int radius,
		int duration,
		int interval,
		List<OnHitEffect> effects,
		List<EntityFilter> filters,
		OwnerMode ownerMode
) implements ContextualEffectHandler {
	public static final String TYPE = "forgero:create_zone";
	public static final int MAX_RADIUS = 32;

	public static final Codec<CreateZoneHandler> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.fieldOf("radius").forGetter(CreateZoneHandler::radius),
			Codec.INT.fieldOf("duration").forGetter(CreateZoneHandler::duration),
			Codec.INT.optionalFieldOf("interval", 20).forGetter(CreateZoneHandler::interval),
			Codec.list(OnHitEffect.CODEC).fieldOf("effects").forGetter(CreateZoneHandler::effects),
			Codec.list(EntityFilter.CODEC).optionalFieldOf("filters", Collections.emptyList()).forGetter(CreateZoneHandler::filters),
			OwnerMode.CODEC.optionalFieldOf("owner_mode", OwnerMode.KEEP_OWNER).forGetter(CreateZoneHandler::ownerMode)
	).apply(instance, CreateZoneHandler::new));

	public CreateZoneHandler {
		if (radius <= 0 || radius > MAX_RADIUS) {
			throw new IllegalArgumentException("radius must be in (0, " + MAX_RADIUS + "], got: " + radius);
		}
		if (duration <= 0) {
			throw new IllegalArgumentException("duration must be > 0, got: " + duration);
		}
		if (interval <= 0) {
			throw new IllegalArgumentException("interval must be > 0, got: " + interval);
		}
	}

	@Override
	public void apply(Entity source, Entity target) {
		World world = target.getWorld();
		if (world.isClient()) {
			return;
		}
		ZoneManager.Zone zone = new ZoneManager.Zone(
				world.getRegistryKey().getValue().toString(),
				target.getPos(),
				radius,
				world.getTime() + duration,
				interval,
				effects,
				filters,
				source != null ? source.getUuid() : null,
				ownerMode);
		ZoneManager.register(zone);
	}

	@Override
	public String type() {
		return TYPE;
	}
}
