package com.sigmundgranaas.forgero.properties.gametest;

import java.util.Optional;

import com.sigmundgranaas.forgero.common.runtime.EvaluableCondition;
import com.sigmundgranaas.forgero.predicate.minecraft.entity.EntityPredicate;
import com.sigmundgranaas.forgero.predicate.minecraft.entity.EntityStatsPredicate;
import com.sigmundgranaas.forgero.predicate.minecraft.standalone.BackstabPredicate;
import com.sigmundgranaas.forgero.predicate.minecraft.util.NumericPredicate;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

/**
 * Proves predicates GATE real effects in both branches, instead of only calling
 * {@code predicate.test(fabricatedContext)} in isolation (the gap the audit flagged for the whole
 * predicate suite). Each test fires a predicate-gated fire effect through the real on-hit dispatch
 * via {@link EffectGatingTestHelper} and asserts the effect lands when the condition holds and is
 * withheld when it does not.
 */
public class PredicateGatingGametest implements FabricGameTest {

	/** Spawns a target facing +Z (south), so its rear cone points toward smaller Z. */
	private static LivingEntity targetFacingSouth(TestContext context, int x, int y, int z) {
		LivingEntity target = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(x, y, z));
		target.setYaw(0.0f);
		target.setHeadYaw(0.0f);
		target.setBodyYaw(0.0f);
		target.setFireTicks(0);
		return target;
	}

	private static ServerPlayerEntity attackerAt(TestContext context, int x, int y, int z) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		player.refreshPositionAndAngles(context.getAbsolutePos(new BlockPos(x, y, z)), 0.0f, 0.0f);
		return player;
	}

	/** Backstab condition true: attacker behind a +Z-facing target (smaller Z) → effect fires. */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, required = true)
	public void backstab_gates_fire_when_attacking_from_behind(TestContext context) {
		LivingEntity target = targetFacingSouth(context, 3, 2, 3);
		ServerPlayerEntity behind = attackerAt(context, 3, 2, 1);
		boolean fired = EffectGatingTestHelper.gatedFireFires(behind, target, new BackstabPredicate(90.0f));
		context.assertTrue(fired, "Backstab-gated fire MUST fire when attacking from behind the target");
		context.complete();
	}

	/** Backstab condition false: attacker in front of the target (larger Z) → effect withheld. */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, required = true)
	public void backstab_withholds_fire_when_attacking_from_front(TestContext context) {
		LivingEntity target = targetFacingSouth(context, 3, 2, 3);
		ServerPlayerEntity front = attackerAt(context, 3, 2, 5);
		boolean fired = EffectGatingTestHelper.gatedFireFires(front, target, new BackstabPredicate(90.0f));
		context.assertFalse(fired, "Backstab-gated fire MUST be withheld when attacking from the front");
		context.complete();
	}

	/** "Execute" gate: the target's health is at or below the threshold (forgero:entity stats.health). */
	private static EvaluableCondition executeBelow(double maxHealth) {
		EntityStatsPredicate stats = new EntityStatsPredicate(
				Optional.of(new NumericPredicate(Optional.empty(), Optional.of(maxHealth), Optional.empty())),
				Optional.empty());
		return new EntityPredicate(
				Optional.of(EntityPredicate.Target.TARGET_ENTITY),
				Optional.empty(), Optional.empty(), Optional.of(stats),
				Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
	}

	/** Execute condition true: target below the HP threshold → effect fires. */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, required = true)
	public void execute_gates_fire_on_low_health_target(TestContext context) {
		ServerPlayerEntity attacker = attackerAt(context, 3, 2, 1);
		LivingEntity target = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(3, 2, 3));
		target.setHealth(4.0f); // <= 6 threshold
		target.setFireTicks(0);
		boolean fired = EffectGatingTestHelper.gatedFireFires(attacker, target, executeBelow(6.0));
		context.assertTrue(fired, "Execute-gated fire MUST fire when the target is at/below the HP threshold");
		context.complete();
	}

	/** Execute condition false: target above the HP threshold → effect withheld. */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, required = true)
	public void execute_withholds_fire_on_healthy_target(TestContext context) {
		ServerPlayerEntity attacker = attackerAt(context, 3, 2, 1);
		LivingEntity target = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(3, 2, 3));
		target.setHealth(20.0f); // > 6 threshold
		target.setFireTicks(0);
		boolean fired = EffectGatingTestHelper.gatedFireFires(attacker, target, executeBelow(6.0));
		context.assertFalse(fired, "Execute-gated fire MUST be withheld when the target is above the HP threshold");
		context.complete();
	}
}
