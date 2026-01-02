package com.sigmundgranaas.forgero.mc.testcommon;

import com.sigmundgranaas.forgero.mc.testcommon.scenario.ScenarioBuilder;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

/**
 * Example tests demonstrating the ScenarioBuilder API.
 * <p>
 * These tests show how to write concise, readable gameplay tests using
 * the fluent ScenarioBuilder API instead of manual setup.
 */
public class ScenarioBuilderExampleTest {

	/**
	 * Example 1: Basic attack scenario.
	 * <p>
	 * Before ScenarioBuilder (verbose, lots of boilerplate):
	 * <pre>{@code
	 * @GameTest
	 * public void attackTest_oldStyle(TestContext context) {
	 *     // Create player
	 *     ServerPlayerEntity player = context.createMockServerPlayerInWorld();
	 *     BlockPos playerPos = context.getAbsolutePos(new BlockPos(1, 64, 1));
	 *     player.refreshPositionAndAngles(playerPos, Direction.SOUTH.asRotation(), 0);
	 *     player.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.DIAMOND_SWORD));
	 *     player.changeGameMode(GameMode.SURVIVAL);
	 *
	 *     // Create target
	 *     PigEntity target = context.spawnEntity(EntityType.PIG, new BlockPos(2, 64, 2));
	 *     target.setHealth(20.0f);
	 *
	 *     // Perform attack
	 *     player.attack(target);
	 *
	 *     // Wait and verify
	 *     context.waitAndRun(3, () -> {
	 *         float damage = 7.0f; // Diamond sword damage
	 *         context.assertTrue(target.getHealth() <= 13.0f, "Target should take damage");
	 *         context.complete();
	 *     });
	 * }
	 * }</pre>
	 *
	 * After ScenarioBuilder (concise, declarative):
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void attackScenario_damagesTarget(TestContext context) {
		ScenarioBuilder.create(context)
			.at(1, 64, 1)
			.holding(new ItemStack(Items.DIAMOND_SWORD))
			.survival()
			.target()
				.entity(EntityType.PIG)
				.at(2, 64, 2)
				.health(20.0f)
			.action()
				.attack()
			.expect()
				.targetHealth(13.0f, 1.0f) // Should take ~7 damage from diamond sword
			.verify();
	}

	/**
	 * Example 2: Testing a hypothetical fire sword.
	 * <p>
	 * This demonstrates testing OnHit fire effects.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void fireSword_setsTargetOnFire(TestContext context) {
		// Note: This assumes you have a fire sword item
		// Replace with actual item from your test data
		ItemStack fireSword = new ItemStack(Items.DIAMOND_SWORD);

		ScenarioBuilder.create(context)
			.at(1, 64, 1)
			.holding(fireSword)
			.target()
				.entity(EntityType.ZOMBIE)
				.at(2, 64, 2)
			.action()
				.attack()
			.expect()
				.targetOnFire(100) // 5 seconds = 100 ticks
			.verify();
	}

	/**
	 * Example 3: Testing a hypothetical poison sword.
	 * <p>
	 * This demonstrates testing OnHit status effects.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void poisonSword_appliesPoisonEffect(TestContext context) {
		ItemStack poisonSword = new ItemStack(Items.DIAMOND_SWORD);

		ScenarioBuilder.create(context)
			.at(1, 64, 1)
			.holding(poisonSword)
			.target()
				.entity(EntityType.COW)
				.at(2, 64, 2)
			.action()
				.attack()
			.expect()
				.targetHasEffect(StatusEffects.POISON, 2, 200) // Poison II for 10 seconds
			.verify();
	}

	/**
	 * Example 4: Testing a life-steal weapon.
	 * <p>
	 * This demonstrates testing effects on both target and player.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void lifeStealSword_healsPlayer(TestContext context) {
		ItemStack lifeStealSword = new ItemStack(Items.DIAMOND_SWORD);

		ScenarioBuilder.create(context)
			.at(1, 64, 1)
			.holding(lifeStealSword)
			.health(10.0f) // Start with low health
			.target()
				.entity(EntityType.ZOMBIE)
				.at(2, 64, 2)
			.action()
				.attack()
			.expect()
				.playerHealth(12.0f, 1.0f) // Should heal 2 HP
				.targetHealth(13.0f, 1.0f) // Should damage target
			.verify();
	}

	/**
	 * Example 5: Testing combined effects.
	 * <p>
	 * This demonstrates testing multiple effects simultaneously.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void fireAndPoisonSword_appliesBothEffects(TestContext context) {
		ItemStack comboSword = new ItemStack(Items.NETHERITE_SWORD);

		ScenarioBuilder.create(context)
			.at(1, 64, 1)
			.holding(comboSword)
			.target()
				.entity(EntityType.CREEPER)
				.at(2, 64, 2)
			.action()
				.attack()
			.expect()
				.targetOnFire(100)
				.targetHasEffect(StatusEffects.POISON)
				.targetHealth(12.0f, 2.0f)
			.verify();
	}

	/**
	 * Example 6: Custom assertion.
	 * <p>
	 * This demonstrates using custom assertions for complex scenarios.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void customAssertion_example(TestContext context) {
		ScenarioBuilder.create(context)
			.at(1, 64, 1)
			.holding(new ItemStack(Items.DIAMOND_SWORD))
			.target()
				.entity(EntityType.PIG)
				.at(2, 64, 2)
			.action()
				.attack()
			.expect()
				.custom(() -> {
					// Custom validation logic here
					// You can access state through closures or add methods to builder
				})
			.verify();
	}

	/**
	 * Example 7: Testing passive effects (no action required).
	 * <p>
	 * This demonstrates testing OnTick effects.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void regenerationArmor_healsPlayerOverTime(TestContext context) {
		ItemStack regenHelmet = new ItemStack(Items.DIAMOND_HELMET);

		ScenarioBuilder.create(context)
			.at(1, 64, 1)
			.holding(regenHelmet)
			.health(10.0f)
			.target()
				.entity(EntityType.PIG) // Dummy target (not used)
				.at(2, 64, 2)
			.action()
				.none() // No action, just wait for OnTick
			.expect()
				.after(20) // Wait 1 second for regeneration
				.playerHealth(12.0f, 1.0f) // Should have healed
			.verify();
	}
}
