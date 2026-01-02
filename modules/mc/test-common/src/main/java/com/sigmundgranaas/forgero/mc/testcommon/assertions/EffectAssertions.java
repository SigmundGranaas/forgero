package com.sigmundgranaas.forgero.mc.testcommon.assertions;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.util.math.Vec3d;

/**
 * Fluent assertions for entity effects in GameTests.
 * <p>
 * Provides a chainable API for asserting that entities have expected effects applied:
 * <ul>
 *   <li>Fire effects (isOnFire, fireTicks)</li>
 *   <li>Status effects (poison, regeneration, etc.)</li>
 *   <li>Velocity changes (knockback, pull)</li>
 *   <li>Health changes</li>
 * </ul>
 *
 * <h2>Example Usage</h2>
 * <pre>{@code
 * @GameTest
 * public void fireEffect_appliesCorrectly(TestContext context) {
 *     // ... setup player, target, perform attack ...
 *
 *     // Assert effects
 *     assertEffect(target)
 *         .hasFire(100)
 *         .hasStatusEffect(StatusEffects.POISON, 2, 200)
 *         .hasVelocity(new Vec3d(0.5, 0.2, 0), 0.1);
 *
 *     context.complete();
 * }
 * }</pre>
 */
public class EffectAssertions {

	private final LivingEntity entity;

	private EffectAssertions(LivingEntity entity) {
		this.entity = entity;
	}

	/**
	 * Starts an effect assertion chain for the given entity.
	 *
	 * @param entity The entity to assert effects on
	 * @return An EffectAssertions instance
	 */
	public static EffectAssertions assertEffect(LivingEntity entity) {
		return new EffectAssertions(entity);
	}

	/**
	 * Asserts that the entity is on fire with at least the specified duration.
	 *
	 * @param minTicks Minimum fire duration in ticks
	 * @return This assertion chain
	 * @throws AssertionError if the entity is not on fire or duration is too short
	 */
	public EffectAssertions hasFire(int minTicks) {
		if (!entity.isOnFire()) {
			throw new AssertionError("Entity should be on fire");
		}
		if (entity.getFireTicks() < minTicks) {
			throw new AssertionError(
				"Fire ticks should be at least " + minTicks + ", but was " + entity.getFireTicks()
			);
		}
		return this;
	}

	/**
	 * Asserts that the entity is on fire (any duration).
	 *
	 * @return This assertion chain
	 * @throws AssertionError if the entity is not on fire
	 */
	public EffectAssertions isOnFire() {
		if (!entity.isOnFire()) {
			throw new AssertionError("Entity should be on fire");
		}
		return this;
	}

	/**
	 * Asserts that the entity is NOT on fire.
	 *
	 * @return This assertion chain
	 * @throws AssertionError if the entity is on fire
	 */
	public EffectAssertions isNotOnFire() {
		if (entity.isOnFire()) {
			throw new AssertionError("Entity should not be on fire");
		}
		return this;
	}

	/**
	 * Asserts that the entity has a specific status effect with level and duration.
	 *
	 * @param effect The status effect type
	 * @param level The effect level (amplifier + 1, e.g., level 2 = amplifier 1)
	 * @param minDuration Minimum duration in ticks
	 * @return This assertion chain
	 * @throws AssertionError if the effect is missing or doesn't match criteria
	 */
	public EffectAssertions hasStatusEffect(StatusEffect effect, int level, int minDuration) {
		StatusEffectInstance instance = entity.getStatusEffect(effect);
		if (instance == null) {
			throw new AssertionError("Entity should have " + effect + " effect");
		}

		int actualLevel = instance.getAmplifier() + 1;
		if (actualLevel < level) {
			throw new AssertionError(
				"Effect level should be at least " + level + ", but was " + actualLevel
			);
		}

		if (instance.getDuration() < minDuration) {
			throw new AssertionError(
				"Effect duration should be at least " + minDuration + ", but was " + instance.getDuration()
			);
		}

		return this;
	}

	/**
	 * Asserts that the entity has a specific status effect (any level/duration).
	 *
	 * @param effect The status effect type
	 * @return This assertion chain
	 * @throws AssertionError if the effect is not present
	 */
	public EffectAssertions hasStatusEffect(StatusEffect effect) {
		if (!entity.hasStatusEffect(effect)) {
			throw new AssertionError("Entity should have " + effect + " effect");
		}
		return this;
	}

	/**
	 * Asserts that the entity does NOT have a specific status effect.
	 *
	 * @param effect The status effect type
	 * @return This assertion chain
	 * @throws AssertionError if the effect is present
	 */
	public EffectAssertions doesNotHaveStatusEffect(StatusEffect effect) {
		if (entity.hasStatusEffect(effect)) {
			throw new AssertionError("Entity should not have " + effect + " effect");
		}
		return this;
	}

	/**
	 * Asserts that the entity has a velocity matching the expected value within tolerance.
	 *
	 * @param expected Expected velocity vector
	 * @param tolerance Acceptable difference per component
	 * @return This assertion chain
	 * @throws AssertionError if velocity doesn't match
	 */
	public EffectAssertions hasVelocity(Vec3d expected, double tolerance) {
		Vec3d actual = entity.getVelocity();

		if (Math.abs(actual.x - expected.x) > tolerance) {
			throw new AssertionError(
				"Velocity X should be " + expected.x + " (±" + tolerance + "), but was " + actual.x
			);
		}

		if (Math.abs(actual.y - expected.y) > tolerance) {
			throw new AssertionError(
				"Velocity Y should be " + expected.y + " (±" + tolerance + "), but was " + actual.y
			);
		}

		if (Math.abs(actual.z - expected.z) > tolerance) {
			throw new AssertionError(
				"Velocity Z should be " + expected.z + " (±" + tolerance + "), but was " + actual.z
			);
		}

		return this;
	}

	/**
	 * Asserts that the entity has velocity in a specific direction (ignoring magnitude).
	 *
	 * @param direction Expected direction (will be normalized)
	 * @param minSpeed Minimum speed magnitude
	 * @return This assertion chain
	 * @throws AssertionError if velocity direction doesn't match
	 */
	public EffectAssertions hasVelocityToward(Vec3d direction, double minSpeed) {
		Vec3d actual = entity.getVelocity();
		double actualSpeed = actual.length();

		if (actualSpeed < minSpeed) {
			throw new AssertionError(
				"Velocity magnitude should be at least " + minSpeed + ", but was " + actualSpeed
			);
		}

		Vec3d normalizedActual = actual.normalize();
		Vec3d normalizedExpected = direction.normalize();

		// Check if vectors are roughly parallel (dot product close to 1)
		double dotProduct = normalizedActual.dotProduct(normalizedExpected);
		if (dotProduct < 0.9) { // cos(~25°) ≈ 0.9
			throw new AssertionError(
				"Velocity direction doesn't match expected. " +
				"Expected: " + normalizedExpected + ", Actual: " + normalizedActual +
				" (dot product: " + dotProduct + ")"
			);
		}

		return this;
	}

	/**
	 * Asserts that the entity has a specific health value.
	 *
	 * @param expected Expected health
	 * @param tolerance Acceptable difference
	 * @return This assertion chain
	 * @throws AssertionError if health doesn't match
	 */
	public EffectAssertions hasHealth(float expected, float tolerance) {
		float actual = entity.getHealth();
		if (Math.abs(actual - expected) > tolerance) {
			throw new AssertionError(
				"Health should be " + expected + " (±" + tolerance + "), but was " + actual
			);
		}
		return this;
	}

	/**
	 * Asserts that the entity has a specific health value (no tolerance).
	 *
	 * @param expected Expected health
	 * @return This assertion chain
	 * @throws AssertionError if health doesn't match exactly
	 */
	public EffectAssertions hasHealth(float expected) {
		return hasHealth(expected, 0.01f);
	}

	/**
	 * Asserts that the entity's health decreased by at least the specified amount.
	 *
	 * @param initialHealth The health before the action
	 * @param minDamage Minimum damage that should have been dealt
	 * @return This assertion chain
	 * @throws AssertionError if damage is less than expected
	 */
	public EffectAssertions tookDamage(float initialHealth, float minDamage) {
		float actual = entity.getHealth();
		float damageTaken = initialHealth - actual;

		if (damageTaken < minDamage) {
			throw new AssertionError(
				"Entity should have taken at least " + minDamage + " damage, but only took " + damageTaken
			);
		}

		return this;
	}

	/**
	 * Asserts that the entity's health increased by at least the specified amount.
	 *
	 * @param initialHealth The health before the action
	 * @param minHealing Minimum healing that should have occurred
	 * @return This assertion chain
	 * @throws AssertionError if healing is less than expected
	 */
	public EffectAssertions wasHealed(float initialHealth, float minHealing) {
		float actual = entity.getHealth();
		float healingReceived = actual - initialHealth;

		if (healingReceived < minHealing) {
			throw new AssertionError(
				"Entity should have been healed by at least " + minHealing + ", but only received " + healingReceived
			);
		}

		return this;
	}

	/**
	 * Asserts that the entity is dead.
	 *
	 * @return This assertion chain
	 * @throws AssertionError if the entity is alive
	 */
	public EffectAssertions isDead() {
		if (!entity.isDead() && entity.getHealth() > 0) {
			throw new AssertionError("Entity should be dead");
		}
		return this;
	}

	/**
	 * Asserts that the entity is alive.
	 *
	 * @return This assertion chain
	 * @throws AssertionError if the entity is dead
	 */
	public EffectAssertions isAlive() {
		if (entity.isDead() || entity.getHealth() <= 0) {
			throw new AssertionError("Entity should be alive");
		}
		return this;
	}
}
