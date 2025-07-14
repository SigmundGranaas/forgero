package com.sigmundgranaas.forgero.predicate.minecraft.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

/**
 * A reusable predicate for checking numeric values. It supports ranges and exact values.
 * This is a fundamental building block for many other predicates.
 *
 * <p><h3>Examples:</h3>
 * {@code "health": { "min": 10.5 }} -> value >= 10.5
 * <br>
 * {@code "y": { "max": 64 }} -> value <= 64
 * <br>
 * {@code "light": { "equal": 15 }} -> value == 15
 */
public record NumericPredicate(
		Optional<Double> min,
		Optional<Double> max,
		Optional<Double> equal
) {
	public static final Codec<NumericPredicate> CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					Codec.DOUBLE.optionalFieldOf("min").forGetter(NumericPredicate::min),
					Codec.DOUBLE.optionalFieldOf("max").forGetter(NumericPredicate::max),
					Codec.DOUBLE.optionalFieldOf("equal").forGetter(NumericPredicate::equal)
			).apply(instance, NumericPredicate::new)
	);

	public boolean test(double value) {
		boolean minMatch = min.map(m -> value >= m).orElse(true);
		boolean maxMatch = max.map(m -> value <= m).orElse(true);
		boolean equalMatch = equal.map(e -> value == e).orElse(true);
		return minMatch && maxMatch && equalMatch;
	}
}
