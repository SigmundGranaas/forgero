package com.sigmundgranaas.forgero.properties.minecraft.entityfilter;

import com.mojang.serialization.Codec;
import net.minecraft.entity.Entity;

/**
 * Filters entities that are alive (not dead or removed).
 */
public record IsAliveFilter() implements EntityFilter {
	public static final String TYPE = "forgero:is_alive";
	public static final IsAliveFilter INSTANCE = new IsAliveFilter();
	public static final Codec<IsAliveFilter> CODEC = Codec.unit(INSTANCE);

	@Override
	public boolean test(Entity source, Entity candidate) {
		return candidate.isAlive();
	}

	@Override
	public String type() {
		return TYPE;
	}
}
