package com.sigmundgranaas.forgero.mc.testcommon.scenario;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder for configuring expectations in a gameplay scenario.
 * <p>
 * Provides a fluent API for declaring what should happen after an action:
 * <ul>
 *   <li>Entity effects (fire, status effects, velocity changes)</li>
 *   <li>Health changes (damage, healing)</li>
 *   <li>Movement (knockback, displacement)</li>
 *   <li>Block states</li>
 *   <li>Death/survival</li>
 *   <li>Custom assertions</li>
 * </ul>
 */
public class ExpectationBuilder {

	private final TestContext context;
	private final ScenarioBuilder.ScenarioState state;

	private final List<Runnable> assertions = new ArrayList<>();
	private int waitTicks = 3; // Default wait time

	// Captured initial state
	private Float initialPlayerHealth = null;
	private Float initialTargetHealth = null;
	private Vec3d initialPlayerPos = null;
	private Vec3d initialTargetPos = null;

	ExpectationBuilder(TestContext context, ScenarioBuilder.ScenarioState state) {
		this.context = context;
		this.state = state;
	}

	/**
	 * Expects the target to be on fire for the specified duration.
	 *
	 * @param ticks Fire duration in ticks
	 * @return This builder
	 */
	public ExpectationBuilder targetOnFire(int ticks) {
		assertions.add(() -> {
			if (state.target == null) {
				throw new IllegalStateException("Target must be configured");
			}
			context.assertTrue(state.target.entity().isOnFire(),
				"Target should be on fire");
			context.assertTrue(state.target.entity().getFireTicks() >= ticks,
				"Fire ticks should be at least " + ticks + ", but was " + state.target.entity().getFireTicks());
		});
		return this;
	}

	/**
	 * Expects the target to have a specific status effect.
	 *
	 * @param effect The status effect
	 * @param level The effect level (amplifier)
	 * @param minDuration Minimum duration in ticks
	 * @return This builder
	 */
	public ExpectationBuilder targetHasEffect(StatusEffect effect, int level, int minDuration) {
		assertions.add(() -> {
			if (state.target == null) {
				throw new IllegalStateException("Target must be configured");
			}
			StatusEffectInstance instance = state.target.entity().getStatusEffect(effect);
			context.assertTrue(instance != null,
				"Target should have " + effect + " effect");
			context.assertTrue(instance.getAmplifier() >= level - 1,
				"Effect level should be at least " + level + ", but was " + (instance.getAmplifier() + 1));
			context.assertTrue(instance.getDuration() >= minDuration,
				"Effect duration should be at least " + minDuration + ", but was " + instance.getDuration());
		});
		return this;
	}

	/**
	 * Expects the target to have a specific status effect (any level/duration).
	 *
	 * @param effect The status effect
	 * @return This builder
	 */
	public ExpectationBuilder targetHasEffect(StatusEffect effect) {
		assertions.add(() -> {
			if (state.target == null) {
				throw new IllegalStateException("Target must be configured");
			}
			context.assertTrue(state.target.entity().hasStatusEffect(effect),
				"Target should have " + effect + " effect");
		});
		return this;
	}

	/**
	 * Expects the target to have specific health.
	 *
	 * @param health Expected health value
	 * @param tolerance Acceptable difference
	 * @return This builder
	 */
	public ExpectationBuilder targetHealth(float health, float tolerance) {
		assertions.add(() -> {
			if (state.target == null) {
				throw new IllegalStateException("Target must be configured");
			}
			float actualHealth = state.target.entity().getHealth();
			context.assertTrue(Math.abs(actualHealth - health) <= tolerance,
				"Target health should be " + health + " (±" + tolerance + "), but was " + actualHealth);
		});
		return this;
	}

	/**
	 * Expects the target to have specific health (no tolerance).
	 *
	 * @param health Expected health value
	 * @return This builder
	 */
	public ExpectationBuilder targetHealth(float health) {
		return targetHealth(health, 0.01f);
	}

	/**
	 * Expects the target to be dead.
	 *
	 * @return This builder
	 */
	public ExpectationBuilder targetDead() {
		assertions.add(() -> {
			if (state.target == null) {
				throw new IllegalStateException("Target must be configured");
			}
			context.assertTrue(state.target.entity().isDead() || state.target.entity().getHealth() <= 0,
				"Target should be dead");
		});
		return this;
	}

	/**
	 * Expects the player to have specific health.
	 *
	 * @param health Expected health value
	 * @param tolerance Acceptable difference
	 * @return This builder
	 */
	public ExpectationBuilder playerHealth(float health, float tolerance) {
		assertions.add(() -> {
			if (state.player == null) {
				throw new IllegalStateException("Player must be configured");
			}
			float actualHealth = state.player.player().getHealth();
			context.assertTrue(Math.abs(actualHealth - health) <= tolerance,
				"Player health should be " + health + " (±" + tolerance + "), but was " + actualHealth);
		});
		return this;
	}

	/**
	 * Adds a custom assertion.
	 *
	 * @param assertion The assertion to run
	 * @return This builder
	 */
	public ExpectationBuilder custom(Runnable assertion) {
		assertions.add(assertion);
		return this;
	}

	/**
	 * Captures the initial state of player and target before the action.
	 * <p>
	 * This allows comparing state changes (damage, healing, movement) after the action.
	 *
	 * @return This builder
	 */
	public ExpectationBuilder captureInitialState() {
		if (state.player != null) {
			initialPlayerHealth = state.player.player().getHealth();
			initialPlayerPos = state.player.player().getPos();
		}
		if (state.target != null) {
			initialTargetHealth = state.target.entity().getHealth();
			initialTargetPos = state.target.entity().getPos();
		}
		return this;
	}

	/**
	 * Expects the player to have taken damage (health decreased).
	 * <p>
	 * Note: Call {@code captureInitialState()} before this to capture initial health,
	 * or provide the initial health explicitly.
	 *
	 * @param minDamage Minimum damage expected
	 * @param tolerance Acceptable difference
	 * @return This builder
	 */
	public ExpectationBuilder playerTookDamage(float minDamage, float tolerance) {
		assertions.add(() -> {
			if (state.player == null) {
				throw new IllegalStateException("Player must be configured");
			}
			if (initialPlayerHealth == null) {
				throw new IllegalStateException("Initial player health not captured - call captureInitialState() first");
			}
			float actualHealth = state.player.player().getHealth();
			float damageTaken = initialPlayerHealth - actualHealth;
			context.assertTrue(damageTaken >= minDamage - tolerance,
				"Player should have taken at least " + minDamage + " damage, but took " + damageTaken);
		});
		return this;
	}

	/**
	 * Expects the player to have taken specific damage with known initial health.
	 *
	 * @param initialHealth The player's initial health
	 * @param minDamage Minimum damage expected
	 * @param tolerance Acceptable difference
	 * @return This builder
	 */
	public ExpectationBuilder playerTookDamage(float initialHealth, float minDamage, float tolerance) {
		this.initialPlayerHealth = initialHealth;
		return playerTookDamage(minDamage, tolerance);
	}

	/**
	 * Expects the target to have moved by at least a minimum distance (for knockback tests).
	 *
	 * @param minDistance Minimum distance in blocks
	 * @return This builder
	 */
	public ExpectationBuilder targetMoved(double minDistance) {
		assertions.add(() -> {
			if (state.target == null) {
				throw new IllegalStateException("Target must be configured");
			}
			if (initialTargetPos == null) {
				throw new IllegalStateException("Initial target position not captured - call captureInitialState() first");
			}
			Vec3d currentPos = state.target.entity().getPos();
			double actualDistance = currentPos.distanceTo(initialTargetPos);
			context.assertTrue(actualDistance >= minDistance,
				"Target should have moved at least " + minDistance + " blocks, but moved " + actualDistance);
		});
		return this;
	}

	/**
	 * Expects a block at the specified position to match the expected state.
	 *
	 * @param pos Block position (relative to test structure)
	 * @param expectedBlock Expected block type
	 * @return This builder
	 */
	public ExpectationBuilder expectBlock(BlockPos pos, Block expectedBlock) {
		assertions.add(() -> {
			BlockPos absolutePos = context.getAbsolutePos(pos);
			BlockState actualState = context.getWorld().getBlockState(absolutePos);
			context.assertTrue(actualState.isOf(expectedBlock),
				"Block at " + pos + " should be " + expectedBlock + ", but was " + actualState.getBlock());
		});
		return this;
	}

	/**
	 * Expects a block at the specified position to match the expected state exactly.
	 *
	 * @param pos Block position (relative to test structure)
	 * @param expectedState Expected block state
	 * @return This builder
	 */
	public ExpectationBuilder expectBlockState(BlockPos pos, BlockState expectedState) {
		assertions.add(() -> {
			BlockPos absolutePos = context.getAbsolutePos(pos);
			BlockState actualState = context.getWorld().getBlockState(absolutePos);
			context.assertTrue(actualState.equals(expectedState),
				"Block state at " + pos + " should be " + expectedState + ", but was " + actualState);
		});
		return this;
	}

	/**
	 * Sets the number of ticks to wait before verifying expectations.
	 * <p>
	 * Default is 3 ticks. Use this to wait for effects that take time to apply.
	 *
	 * @param ticks Number of ticks to wait
	 * @return This builder
	 */
	public ExpectationBuilder after(int ticks) {
		this.waitTicks = ticks;
		return this;
	}

	/**
	 * Executes the scenario: performs the action and verifies expectations.
	 * <p>
	 * This completes the scenario test.
	 */
	public void verify() {
		// Perform the action
		if (state.action != null) {
			state.action.action().run();
		}

		// Wait for effects to apply
		context.waitAndRun(waitTicks, () -> {
			// Run all assertions
			for (Runnable assertion : assertions) {
				assertion.run();
			}

			// Complete the test
			context.complete();
		});
	}
}
