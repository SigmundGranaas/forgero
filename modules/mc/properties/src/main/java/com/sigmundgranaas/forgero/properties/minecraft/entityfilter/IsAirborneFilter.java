package com.sigmundgranaas.forgero.properties.minecraft.entityfilter;

import com.mojang.serialization.Codec;
import net.minecraft.entity.Entity;

/**
 * Selects only entities that are not on the ground (jumping, falling, flying). Useful for aerial
 * combat bonuses.
 */
public record IsAirborneFilter() implements EntityFilter {
	public static final String TYPE = "forgero:is_airborne";
	public static final IsAirborneFilter INSTANCE = new IsAirborneFilter();
	public static final Codec<IsAirborneFilter> CODEC = Codec.unit(INSTANCE);

	@Override
	public boolean test(Entity source, Entity candidate) {
		return !candidate.isOnGround();
	}

	@Override
	public String type() {
		return TYPE;
	}
}
