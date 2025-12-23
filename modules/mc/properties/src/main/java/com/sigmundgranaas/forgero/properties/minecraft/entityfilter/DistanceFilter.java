package com.sigmundgranaas.forgero.properties.minecraft.entityfilter;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.Entity;

/**
 * Filters entities based on their distance from the source entity.
 * Useful for range-limited effects or scaling effects by distance.
 */
public record DistanceFilter(float min, float max) implements EntityFilter {
	public DistanceFilter {
		if (min < 0) {
			throw new IllegalArgumentException("min distance must be >= 0, got: " + min);
		}
		if (max < min) {
			throw new IllegalArgumentException("max distance must be >= min (" + min + "), got: " + max);
		}
	}

	public static final String TYPE = "forgero:distance";
	public static final Codec<DistanceFilter> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.FLOAT.optionalFieldOf("min", 0.0f).forGetter(DistanceFilter::min),
			Codec.FLOAT.fieldOf("max").forGetter(DistanceFilter::max)
	).apply(instance, DistanceFilter::new));

	@Override
	public boolean test(Entity source, Entity candidate) {
		float distance = source.distanceTo(candidate);
		return distance >= min && distance <= max;
	}

	@Override
	public String type() {
		return TYPE;
	}
}
