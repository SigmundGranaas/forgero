package com.sigmundgranaas.forgero.properties.minecraft.entityfilter;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

import java.util.Optional;

/**
 * Filters entities based on their current state such as burning, sprinting, airborne, or health.
 * All fields are optional - at least one should be specified for the filter to be meaningful.
 *
 * <h3>JSON Configuration Example:</h3>
 * <pre>
 * {
 *   "type": "forgero:entity_state",
 *   "is_burning": true,
 *   "is_sprinting": true,
 *   "is_airborne": false,
 *   "health_percentage_max": 0.5
 * }
 * </pre>
 *
 * <h3>Use Cases:</h3>
 * <ul>
 *   <li>Combo mechanics (extra damage vs burning enemies)</li>
 *   <li>Execute mechanics (bonus damage to low-health targets)</li>
 *   <li>Anti-air effects (bonus damage to airborne enemies)</li>
 *   <li>Berserker effects (activated when player is low health)</li>
 * </ul>
 *
 * <h3>Performance Notes:</h3>
 * State checks are very lightweight as they query simple entity properties.
 *
 * @param isBurning Optional burning check (true = must be on fire, false = must not be on fire)
 * @param isSprinting Optional sprinting check (true = must be sprinting, false = must not be sprinting)
 * @param isAirborne Optional airborne check (true = must be airborne, false = must be grounded)
 * @param healthPercentageMax Optional health percentage threshold (0.0-1.0, entity must be at or below this percentage)
 */
public record EntityStateFilter(
		Optional<Boolean> isBurning,
		Optional<Boolean> isSprinting,
		Optional<Boolean> isAirborne,
		Optional<Double> healthPercentageMax
) implements EntityFilter {
	/**
	 * Compact constructor for validation.
	 */
	public EntityStateFilter {
		// Validate health percentage if present
		if (healthPercentageMax.isPresent()) {
			double value = healthPercentageMax.get();
			if (value < 0.0 || value > 1.0) {
				throw new IllegalArgumentException("health_percentage_max must be between 0.0 and 1.0, got: " + value);
			}
		}
	}

	public static final String TYPE = "forgero:entity_state";
	public static final Codec<EntityStateFilter> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.BOOL.optionalFieldOf("is_burning").forGetter(EntityStateFilter::isBurning),
			Codec.BOOL.optionalFieldOf("is_sprinting").forGetter(EntityStateFilter::isSprinting),
			Codec.BOOL.optionalFieldOf("is_airborne").forGetter(EntityStateFilter::isAirborne),
			Codec.DOUBLE.optionalFieldOf("health_percentage_max").forGetter(EntityStateFilter::healthPercentageMax)
	).apply(instance, EntityStateFilter::new));

	@Override
	public boolean test(Entity source, Entity candidate) {
		// Check burning
		if (isBurning.isPresent()) {
			if (candidate.isOnFire() != isBurning.get()) {
				return false;
			}
		}

		// Check sprinting
		if (isSprinting.isPresent()) {
			boolean sprinting = candidate.isSprinting();
			if (sprinting != isSprinting.get()) {
				return false;
			}
		}

		// Check airborne
		if (isAirborne.isPresent()) {
			boolean airborne = !candidate.isOnGround();
			if (airborne != isAirborne.get()) {
				return false;
			}
		}

		// Check health percentage
		if (healthPercentageMax.isPresent() && candidate instanceof LivingEntity living) {
			double healthPercentage = living.getHealth() / living.getMaxHealth();
			if (healthPercentage > healthPercentageMax.get()) {
				return false;
			}
		} else if (healthPercentageMax.isPresent()) {
			// If health check is required but entity is not LivingEntity, fail the filter
			return false;
		}

		return true;
	}

	@Override
	public String type() {
		return TYPE;
	}
}
