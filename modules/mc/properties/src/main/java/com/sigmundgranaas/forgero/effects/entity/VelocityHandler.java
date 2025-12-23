package com.sigmundgranaas.forgero.effects.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.Vec3d;

import java.util.Locale;

/**
 * Manipulates entity velocity for dashing, knockback, recoil, and launching effects.
 * Provides flexible velocity control with multiple modes and targets.
 *
 * <h3>JSON Configuration Examples:</h3>
 *
 * <p><b>Dashing (Forward Movement):</b></p>
 * <pre>
 * {
 *   "type": "forgero:velocity",
 *   "target": "self",
 *   "amount": 2.0,
 *   "mode": "add",
 *   "vertical_bias": 0.3
 * }
 * </pre>
 *
 * <p><b>Enhanced Knockback:</b></p>
 * <pre>
 * {
 *   "type": "forgero:velocity",
 *   "target": "target",
 *   "amount": 1.5,
 *   "mode": "away_from_source",
 *   "vertical_bias": 0.5
 * }
 * </pre>
 *
 * <p><b>Launch Attack:</b></p>
 * <pre>
 * {
 *   "type": "forgero:velocity",
 *   "target": "target",
 *   "amount": 2.0,
 *   "mode": "set",
 *   "vertical_bias": 1.5
 * }
 * </pre>
 *
 * <h3>Modes:</h3>
 * <ul>
 *   <li><b>ADD</b> - Adds velocity to current velocity (best for dashing)</li>
 *   <li><b>SET</b> - Replaces current velocity (best for launches)</li>
 *   <li><b>AWAY_FROM_SOURCE</b> - Applies velocity away from source entity (best for knockback)</li>
 * </ul>
 *
 * <h3>Targets:</h3>
 * <ul>
 *   <li><b>SELF</b> - Affects the source entity (attacker) using their look direction</li>
 *   <li><b>TARGET</b> - Affects the target entity (victim)</li>
 * </ul>
 *
 * <h3>Performance Notes:</h3>
 * This handler is lightweight and suitable for frequent use.
 * Always sets {@code entity.velocityModified = true} to ensure proper server synchronization.
 *
 * @param target Which entity to affect (SELF or TARGET)
 * @param amount Velocity magnitude/multiplier
 * @param mode How to apply velocity (ADD, SET, AWAY_FROM_SOURCE)
 * @param verticalBias Additional vertical velocity for jumps/launches (can be negative for slam effects)
 */
public record VelocityHandler(VelocityTarget target, double amount, VelocityMode mode, double verticalBias) implements ContextualEffectHandler {
	public static final String TYPE = "forgero:velocity";
	public static final Codec<VelocityHandler> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			VelocityTarget.CODEC.fieldOf("target").forGetter(VelocityHandler::target),
			Codec.DOUBLE.fieldOf("amount").forGetter(VelocityHandler::amount),
			VelocityMode.CODEC.fieldOf("mode").forGetter(VelocityHandler::mode),
			Codec.DOUBLE.optionalFieldOf("vertical_bias", 0.0).forGetter(VelocityHandler::verticalBias)
	).apply(instance, VelocityHandler::new));

	@Override
	public void apply(Entity source, Entity targetEntity) {
		Entity affected = (target == VelocityTarget.SELF) ? source : targetEntity;

		Vec3d velocity = switch (mode) {
			case ADD -> {
				Vec3d direction = calculateDirection(source, targetEntity, affected);
				if (direction == null) yield affected.getVelocity(); // Too close, no change
				yield affected.getVelocity().add(direction.multiply(amount));
			}
			case SET -> {
				Vec3d direction = calculateDirection(source, targetEntity, affected);
				if (direction == null) yield Vec3d.ZERO; // Too close, no velocity
				yield direction.multiply(amount);
			}
			case AWAY_FROM_SOURCE -> {
				Vec3d diff = affected.getPos().subtract(source.getPos());
				if (diff.lengthSquared() < 0.001) {
					yield Vec3d.ZERO; // Too close to source, no knockback
				}
				Vec3d direction = diff.normalize();
				yield direction.multiply(amount);
			}
		};

		// Apply vertical bias
		if (verticalBias != 0.0) {
			velocity = velocity.add(0, verticalBias, 0);
		}

		affected.setVelocity(velocity);
		affected.velocityModified = true; // Critical for server-side synchronization
	}

	/**
	 * Calculates the direction vector based on target and mode.
	 * For SELF-targeted effects, uses the source's look direction.
	 * For TARGET-targeted effects, uses direction from source to target.
	 * Returns null if entities are too close (prevents division by zero).
	 */
	private Vec3d calculateDirection(Entity source, Entity targetEntity, Entity affected) {
		if (affected == source) {
			// For self-targeting, use look direction
			return source.getRotationVec(1.0f);
		} else {
			// For targeting others, use direction from source to target
			Vec3d diff = targetEntity.getPos().subtract(source.getPos());
			if (diff.lengthSquared() < 0.001) {
				return null; // Entities too close, cannot calculate direction
			}
			return diff.normalize();
		}
	}

	@Override
	public String type() {
		return TYPE;
	}

	/**
	 * Determines which entity's velocity should be modified.
	 */
	public enum VelocityTarget implements StringIdentifiable {
		/** Affects the source entity (attacker) - uses look direction for movement */
		SELF,
		/** Affects the target entity (victim) - uses source-to-target direction */
		TARGET;

		public static final Codec<VelocityTarget> CODEC = StringIdentifiable.createCodec(
				VelocityTarget::values,
				(value) -> String.valueOf(VelocityTarget.valueOf(value.toUpperCase(Locale.ROOT)))
		);

		@Override
		public String asString() {
			return this.name().toLowerCase(Locale.ROOT);
		}
	}

	/**
	 * Determines how velocity is applied to the entity.
	 */
	public enum VelocityMode implements StringIdentifiable {
		/** Adds velocity to current velocity - best for dashing/boosting */
		ADD,
		/** Sets velocity, replacing current velocity - best for launches */
		SET,
		/** Applies velocity away from source entity - best for knockback */
		AWAY_FROM_SOURCE;

		public static final Codec<VelocityMode> CODEC = StringIdentifiable.createCodec(
				VelocityMode::values,
				(value) -> String.valueOf(VelocityMode.valueOf(value.toUpperCase(Locale.ROOT)))
		);

		@Override
		public String asString() {
			return this.name().toLowerCase(Locale.ROOT);
		}
	}
}
