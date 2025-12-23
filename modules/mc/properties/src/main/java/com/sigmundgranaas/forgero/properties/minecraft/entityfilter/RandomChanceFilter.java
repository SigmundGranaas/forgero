package com.sigmundgranaas.forgero.properties.minecraft.entityfilter;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.Entity;

/**
 * Filters entities based on random chance.
 * Chance is a value between 0.0 (0% chance) and 1.0 (100% chance).
 * Example: 0.25 = 25% chance to pass the filter
 */
public record RandomChanceFilter(float chance) implements EntityFilter {
	public RandomChanceFilter {
		if (chance < 0.0f || chance > 1.0f) {
			throw new IllegalArgumentException("chance must be between 0.0 and 1.0, got: " + chance);
		}
	}

	public static final String TYPE = "forgero:random_chance";
	public static final Codec<RandomChanceFilter> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.FLOAT.fieldOf("chance").forGetter(RandomChanceFilter::chance)
	).apply(instance, RandomChanceFilter::new));

	@Override
	public boolean test(Entity source, Entity candidate) {
		return candidate.getWorld().getRandom().nextFloat() < chance;
	}

	@Override
	public String type() {
		return TYPE;
	}
}
