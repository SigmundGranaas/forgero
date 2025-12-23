package com.sigmundgranaas.forgero.properties.minecraft.entityfilter;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.StringIdentifiable;

import java.util.Locale;

/**
 * Filters entities based on their health relative to their max health.
 * For example, can filter entities with health below 50% of max health.
 */
public record HealthThresholdFilter(float threshold, Comparator comparator) implements EntityFilter {
	public HealthThresholdFilter {
		if (threshold < 0.0f || threshold > 1.0f) {
			throw new IllegalArgumentException("threshold must be between 0.0 and 1.0, got: " + threshold);
		}
		if (comparator == null) {
			throw new IllegalArgumentException("comparator cannot be null");
		}
	}

	public static final String TYPE = "forgero:health_threshold";
	public static final Codec<HealthThresholdFilter> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.FLOAT.fieldOf("threshold").forGetter(HealthThresholdFilter::threshold),
			Comparator.CODEC.fieldOf("comparator").forGetter(HealthThresholdFilter::comparator)
	).apply(instance, HealthThresholdFilter::new));

	@Override
	public boolean test(Entity source, Entity candidate) {
		if (!(candidate instanceof LivingEntity living)) {
			return false;
		}

		float healthRatio = living.getHealth() / living.getMaxHealth();
		return switch (comparator) {
			case LESS_THAN -> healthRatio < threshold;
			case LESS_THAN_OR_EQUAL -> healthRatio <= threshold;
			case GREATER_THAN -> healthRatio > threshold;
			case GREATER_THAN_OR_EQUAL -> healthRatio >= threshold;
			case EQUAL -> Math.abs(healthRatio - threshold) < 0.01f;
		};
	}

	@Override
	public String type() {
		return TYPE;
	}

	public enum Comparator implements StringIdentifiable {
		LESS_THAN,
		LESS_THAN_OR_EQUAL,
		GREATER_THAN,
		GREATER_THAN_OR_EQUAL,
		EQUAL;

		public static final Codec<Comparator> CODEC = StringIdentifiable.createCodec(
				Comparator::values,
				(value) -> String.valueOf(Comparator.valueOf(value.toUpperCase(Locale.ROOT)))
		);

		@Override
		public String asString() {
			return this.name().toLowerCase(Locale.ROOT);
		}
	}
}
