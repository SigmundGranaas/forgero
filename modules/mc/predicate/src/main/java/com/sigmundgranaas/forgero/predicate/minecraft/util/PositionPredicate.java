package com.sigmundgranaas.forgero.predicate.minecraft.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.Optional;

/**
 * A predicate for checking X, Y, and Z coordinates using {@link NumericPredicate}.
 *
 * <p><h3>Example:</h3>
 * {@code "position": { "y": { "min": 64 }}}
 */
public record PositionPredicate(
		Optional<NumericPredicate> x,
		Optional<NumericPredicate> y,
		Optional<NumericPredicate> z
) {
	public static final Codec<PositionPredicate> CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					NumericPredicate.CODEC.optionalFieldOf("x").forGetter(PositionPredicate::x),
					NumericPredicate.CODEC.optionalFieldOf("y").forGetter(PositionPredicate::y),
					NumericPredicate.CODEC.optionalFieldOf("z").forGetter(PositionPredicate::z)
			).apply(instance, PositionPredicate::new)
	);

	public boolean test(BlockPos pos) {
		// A BlockPos represents a 1x1x1 volume. Test against the center of this volume for robust checks.
		return test(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
	}

	public boolean test(Vec3d pos) {
		return test(pos.getX(), pos.getY(), pos.getZ());
	}

	private boolean test(double x, double y, double z) {
		boolean xMatch = this.x.map(p -> p.test(x)).orElse(true);
		boolean yMatch = this.y.map(p -> p.test(y)).orElse(true);
		boolean zMatch = this.z.map(p -> p.test(z)).orElse(true);
		return xMatch && yMatch && zMatch;
	}
}
