package com.sigmundgranaas.forgero.predicate.minecraft.standalone;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.runtime.DynamicContext;
import com.sigmundgranaas.forgero.common.runtime.EvaluableCondition;
import com.sigmundgranaas.forgero.common.runtime.MinecraftContextKeys;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

/**
 * Dynamic condition: passes when the source is attacking the target from behind — i.e. the source
 * sits inside a rear cone of the target. Compares horizontal facing only (pitch ignored).
 *
 * <pre>{ "type": "forgero:backstab", "max_angle": 90 }</pre>
 *
 * {@code max_angle} (default 90) is the full width of the rear cone in degrees; the source must be
 * within {@code max_angle / 2} of directly behind the target.
 */
public record BackstabPredicate(float maxAngle) implements EvaluableCondition {
	public static final OpenIdentifier TYPE = new OpenIdentifier("forgero", "backstab");

	public static final Codec<BackstabPredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.FLOAT.optionalFieldOf("max_angle", 90.0f).forGetter(BackstabPredicate::maxAngle)
	).apply(instance, BackstabPredicate::new));

	public BackstabPredicate {
		if (maxAngle <= 0 || maxAngle > 360) {
			throw new IllegalArgumentException("max_angle must be between 0 and 360 degrees, got: " + maxAngle);
		}
	}

	@Override
	public boolean test(DynamicContext context) {
		Optional<Entity> sourceOpt = context.get(MinecraftContextKeys.SOURCE_ENTITY);
		Optional<Entity> targetOpt = context.get(MinecraftContextKeys.TARGET_ENTITY);
		if (sourceOpt.isEmpty() || targetOpt.isEmpty()) {
			return false;
		}
		Entity source = sourceOpt.get();
		Entity target = targetOpt.get();

		Vec3d rear = flatten(target.getRotationVector()).multiply(-1.0);
		Vec3d toSource = flatten(source.getPos().subtract(target.getPos()));
		if (rear.lengthSquared() < 1.0e-6 || toSource.lengthSquared() < 1.0e-6) {
			return false;
		}
		double dot = Math.max(-1.0, Math.min(1.0, rear.normalize().dotProduct(toSource.normalize())));
		double angleFromRear = Math.toDegrees(Math.acos(dot));
		return angleFromRear <= maxAngle / 2.0;
	}

	private static Vec3d flatten(Vec3d v) {
		return new Vec3d(v.x, 0.0, v.z);
	}

	@Override
	public OpenIdentifier type() {
		return TYPE;
	}
}
