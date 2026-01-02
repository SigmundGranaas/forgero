package com.sigmundgranaas.forgero.mc.testcommon.scenario;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Collection;
import java.util.Collections;

/**
 * Builder for configuring the action in a gameplay scenario.
 * <p>
 * Provides a fluent API for defining actions such as:
 * <ul>
 *   <li>Attacking entities</li>
 *   <li>Breaking blocks</li>
 *   <li>Shooting projectiles</li>
 *   <li>Target attacking player (for armor tests)</li>
 *   <li>Custom actions</li>
 * </ul>
 */
public class ActionBuilder {

	private final TestContext context;
	private final ScenarioBuilder.ScenarioState state;

	private Runnable action = () -> {};

	ActionBuilder(TestContext context, ScenarioBuilder.ScenarioState state) {
		this.context = context;
		this.state = state;
	}

	/**
	 * Action: Player attacks the target.
	 *
	 * @return This builder
	 */
	public ActionBuilder attack() {
		this.action = () -> {
			if (state.player == null || state.target == null) {
				throw new IllegalStateException("Player and target must be configured before attack");
			}
			state.player.player().attack(state.target.entity());
		};
		return this;
	}

	/**
	 * Action: Player breaks a block.
	 *
	 * @param pos The block position to break
	 * @return This builder
	 */
	public ActionBuilder breakBlock(BlockPos pos) {
		this.action = () -> {
			if (state.player == null) {
				throw new IllegalStateException("Player must be configured before breaking blocks");
			}
			context.getWorld().breakBlock(pos, true, state.player.player());
		};
		return this;
	}

	/**
	 * Action: Custom action defined by a runnable.
	 *
	 * @param customAction The custom action to run
	 * @return This builder
	 */
	public ActionBuilder custom(Runnable customAction) {
		this.action = customAction;
		return this;
	}

	/**
	 * Action: No action (for testing passive effects like OnTick).
	 *
	 * @return This builder
	 */
	public ActionBuilder none() {
		this.action = () -> {};
		return this;
	}

	/**
	 * Action: Target attacks the player (for testing armor protection and thorns).
	 *
	 * @return This builder
	 */
	public ActionBuilder targetAttacksPlayer() {
		this.action = () -> {
			if (state.player == null || state.target == null) {
				throw new IllegalStateException("Player and target must be configured");
			}
			state.target.entity().tryAttack(state.player.player());
		};
		return this;
	}

	/**
	 * Action: Player shoots an arrow at the target.
	 * <p>
	 * This simulates the player using a bow to shoot an arrow at the target entity.
	 * Useful for testing OnHit effects on arrows.
	 *
	 * @return This builder
	 */
	public ActionBuilder shootArrow() {
		this.action = () -> {
			if (state.player == null || state.target == null) {
				throw new IllegalStateException("Player and target must be configured");
			}

			World world = context.getWorld();
			Vec3d playerPos = state.player.player().getPos();
			Vec3d targetPos = state.target.entity().getPos();
			Vec3d direction = targetPos.subtract(playerPos).normalize();

			// Create arrow entity
			ArrowEntity arrow = new ArrowEntity(world, state.player.player());
			arrow.setPos(playerPos.x, playerPos.y + state.player.player().getStandingEyeHeight(), playerPos.z);
			arrow.setVelocity(direction.x, direction.y, direction.z, 3.0f, 0.0f);
			arrow.setOwner(state.player.player());

			// Spawn arrow in world
			world.spawnEntity(arrow);

			// Position arrow very close to target - collision will happen on next tick
			// Note: Direct onEntityHit call not possible due to protected access
			arrow.setPos(targetPos.x, targetPos.y + 1.0, targetPos.z);
		};
		return this;
	}

	/**
	 * Action: Player breaks multiple blocks in a pattern (for vein mining tests).
	 *
	 * @param positions Collection of block positions to break
	 * @return This builder
	 */
	public ActionBuilder breakBlockPattern(Collection<BlockPos> positions) {
		this.action = () -> {
			if (state.player == null) {
				throw new IllegalStateException("Player must be configured");
			}
			for (BlockPos pos : positions) {
				context.getWorld().breakBlock(pos, true, state.player.player());
			}
		};
		return this;
	}

	/**
	 * Action: Wait for a specified number of ticks (for OnTick effects).
	 * <p>
	 * Note: This is a no-op action. Use {@code .expect().after(ticks)} to add delay.
	 *
	 * @param ticks Number of ticks to wait
	 * @return This builder
	 */
	public ActionBuilder wait(int ticks) {
		// This is a no-op - the actual waiting happens in ExpectationBuilder.after()
		this.action = () -> {};
		return this;
	}

	/**
	 * Completes action configuration and moves to expectation setup.
	 *
	 * @return An ExpectationBuilder for configuring expectations
	 */
	public ExpectationBuilder expect() {
		// Store the action in state
		state.action = new ScenarioAction(action);

		return new ExpectationBuilder(context, state);
	}
}
