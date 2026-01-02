package com.sigmundgranaas.forgero.mc.testcommon.scenario;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

/**
 * Builder for configuring the target entity in a gameplay scenario.
 * <p>
 * Provides a fluent API for setting:
 * <ul>
 *   <li>Entity type</li>
 *   <li>Position</li>
 *   <li>Health and status</li>
 * </ul>
 */
public class TargetBuilder {

	private final TestContext context;
	private final ScenarioBuilder.ScenarioState state;

	private EntityType<? extends LivingEntity> entityType = EntityType.PIG;
	private BlockPos position = new BlockPos(2, 64, 2);
	private float health = 20.0f;

	TargetBuilder(TestContext context, ScenarioBuilder.ScenarioState state) {
		this.context = context;
		this.state = state;
	}

	/**
	 * Sets the entity type for the target.
	 *
	 * @param type The entity type
	 * @return This builder
	 */
	public TargetBuilder entity(EntityType<? extends LivingEntity> type) {
		this.entityType = type;
		return this;
	}

	/**
	 * Sets the target's position.
	 *
	 * @param pos The position
	 * @return This builder
	 */
	public TargetBuilder at(BlockPos pos) {
		this.position = pos;
		return this;
	}

	/**
	 * Sets the target's position.
	 *
	 * @param x X coordinate (relative to test structure)
	 * @param y Y coordinate
	 * @param z Z coordinate (relative to test structure)
	 * @return This builder
	 */
	public TargetBuilder at(int x, int y, int z) {
		return at(new BlockPos(x, y, z));
	}

	/**
	 * Sets the target's health.
	 *
	 * @param health Health value
	 * @return This builder
	 */
	public TargetBuilder health(float health) {
		this.health = health;
		return this;
	}

	/**
	 * Completes target configuration and moves to action setup.
	 *
	 * @return An ActionBuilder for configuring the action
	 */
	public ActionBuilder action() {
		// Spawn the target entity
		LivingEntity entity = context.spawnEntity(entityType, position);
		entity.setHealth(health);

		// Store in state
		state.target = new ScenarioTarget(entity);

		return new ActionBuilder(context, state);
	}
}
