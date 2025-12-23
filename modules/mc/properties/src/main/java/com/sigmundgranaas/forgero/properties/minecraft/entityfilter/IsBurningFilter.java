package com.sigmundgranaas.forgero.properties.minecraft.entityfilter;

import com.mojang.serialization.Codec;
import net.minecraft.entity.Entity;

/**
 * Filters entities that are currently on fire.
 */
public record IsBurningFilter() implements EntityFilter {
	public static final String TYPE = "forgero:is_burning";
	public static final IsBurningFilter INSTANCE = new IsBurningFilter();
	public static final Codec<IsBurningFilter> CODEC = Codec.unit(INSTANCE);

	@Override
	public boolean test(Entity source, Entity candidate) {
		return candidate.isOnFire();
	}

	@Override
	public String type() {
		return TYPE;
	}
}
