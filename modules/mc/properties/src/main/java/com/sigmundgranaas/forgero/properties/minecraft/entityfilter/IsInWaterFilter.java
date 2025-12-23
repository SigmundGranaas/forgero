package com.sigmundgranaas.forgero.properties.minecraft.entityfilter;

import com.mojang.serialization.Codec;
import net.minecraft.entity.Entity;

/**
 * Filters entities that are currently in water (including swimming).
 */
public record IsInWaterFilter() implements EntityFilter {
	public static final String TYPE = "forgero:is_in_water";
	public static final IsInWaterFilter INSTANCE = new IsInWaterFilter();
	public static final Codec<IsInWaterFilter> CODEC = Codec.unit(INSTANCE);

	@Override
	public boolean test(Entity source, Entity candidate) {
		return candidate.isTouchingWater();
	}

	@Override
	public String type() {
		return TYPE;
	}
}
