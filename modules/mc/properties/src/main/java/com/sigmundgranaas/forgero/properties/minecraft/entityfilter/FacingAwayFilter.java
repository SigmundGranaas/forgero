package com.sigmundgranaas.forgero.properties.minecraft.entityfilter;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

/**
 * Selects entities that are facing away from the source (the source is in their rear hemisphere).
 * Enables backstab-style targeting from selectors. Compares horizontal facing only (pitch ignored).
 *
 * <p>{@code min_angle} (default 90) is the minimum angle, in degrees, between the candidate's
 * facing and the direction to the source for the candidate to count as "facing away".
 */
public record FacingAwayFilter(float minAngle) implements EntityFilter {
	public static final String TYPE = "forgero:facing_away";

	public static final Codec<FacingAwayFilter> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.FLOAT.optionalFieldOf("min_angle", 90.0f).forGetter(FacingAwayFilter::minAngle)
	).apply(instance, FacingAwayFilter::new));

	public FacingAwayFilter {
		if (minAngle < 0 || minAngle > 180) {
			throw new IllegalArgumentException("min_angle must be between 0 and 180 degrees, got: " + minAngle);
		}
	}

	@Override
	public boolean test(Entity source, Entity candidate) {
		Vec3d look = flatten(candidate.getRotationVector());
		Vec3d toSource = flatten(source.getPos().subtract(candidate.getPos()));
		if (look.lengthSquared() < 1.0e-6 || toSource.lengthSquared() < 1.0e-6) {
			return false;
		}
		double dot = Math.max(-1.0, Math.min(1.0, look.normalize().dotProduct(toSource.normalize())));
		double angle = Math.toDegrees(Math.acos(dot));
		return angle >= minAngle;
	}

	private static Vec3d flatten(Vec3d v) {
		return new Vec3d(v.x, 0.0, v.z);
	}

	@Override
	public String type() {
		return TYPE;
	}
}
