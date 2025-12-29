package com.sigmundgranaas.forgero.properties.gametest;

import com.sigmundgranaas.forgero.properties.minecraft.entityfilter.*;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

import java.util.List;

/**
 * Tests entity filters for effect targeting.
 * Focus: Do filters correctly allow/reject entities?
 * - Alive, hostile, teammate, health, distance, boolean logic, etc.
 */
public class EntityFilterGametest {

	/**
	 * USE CASE: Effect only targets living entities (not dead/removed).
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testIsAliveFilter(TestContext context) {
		IsAliveFilter filter = new IsAliveFilter();
		LivingEntity source = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(0, 1, 0));
		LivingEntity alive = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(1, 1, 0));
		LivingEntity dead = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(2, 1, 0));

		dead.setHealth(0.0f);
		dead.kill();

		context.assertTrue(filter.test(source, alive), "Should pass for alive entity");
		context.assertFalse(filter.test(source, dead), "Should fail for dead entity");
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testIsHostileFilter(TestContext context) {
		IsHostileFilter filter = new IsHostileFilter();
		LivingEntity source = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(0, 1, 0));
		ZombieEntity hostile = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(1, 1, 0));
		VillagerEntity peaceful = context.spawnEntity(EntityType.VILLAGER, new BlockPos(2, 1, 0));

		context.assertTrue(filter.test(source, hostile), "Should pass for hostile entity");
		context.assertFalse(filter.test(source, peaceful), "Should fail for peaceful entity");
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testIsTeammateFilter(TestContext context) {
		IsTeammateFilter filter = new IsTeammateFilter(false);
		IsTeammateFilter invertedFilter = new IsTeammateFilter(true);

		LivingEntity source = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(0, 1, 0));
		LivingEntity teammate = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(1, 1, 0));
		LivingEntity enemy = context.spawnEntity(EntityType.VILLAGER, new BlockPos(2, 1, 0));

		// Zombies consider other zombies as teammates (same team)
		boolean isTeammate = teammate.isTeammate(source);

		context.assertTrue(filter.test(source, teammate) == isTeammate, "Should pass for teammate when not inverted");
		context.assertTrue(invertedFilter.test(source, teammate) == !isTeammate, "Should fail for teammate when inverted");
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testHealthThresholdFilter(TestContext context) {
		HealthThresholdFilter filterBelow50 = new HealthThresholdFilter(0.5f, HealthThresholdFilter.Comparator.LESS_THAN);
		HealthThresholdFilter filterAbove50 = new HealthThresholdFilter(0.5f, HealthThresholdFilter.Comparator.GREATER_THAN);

		LivingEntity source = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(0, 1, 0));
		LivingEntity lowHealth = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(1, 1, 0));
		LivingEntity highHealth = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(2, 1, 0));

		// Set health to 25% of max
		lowHealth.setHealth(lowHealth.getMaxHealth() * 0.25f);
		// Set health to 75% of max
		highHealth.setHealth(highHealth.getMaxHealth() * 0.75f);

		context.assertTrue(filterBelow50.test(source, lowHealth), "Low health entity should pass LESS_THAN 0.5 filter");
		context.assertFalse(filterBelow50.test(source, highHealth), "High health entity should not pass LESS_THAN 0.5 filter");

		context.assertFalse(filterAbove50.test(source, lowHealth), "Low health entity should not pass GREATER_THAN 0.5 filter");
		context.assertTrue(filterAbove50.test(source, highHealth), "High health entity should pass GREATER_THAN 0.5 filter");

		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testDistanceFilter(TestContext context) {
		DistanceFilter filter = new DistanceFilter(0.0f, 3.0f);

		LivingEntity source = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(0, 1, 0));
		LivingEntity near = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(2, 1, 0)); // ~2 blocks away
		LivingEntity far = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(5, 1, 0)); // ~5 blocks away

		context.assertTrue(filter.test(source, near), "Near entity should pass distance filter");
		context.assertFalse(filter.test(source, far), "Far entity should not pass distance filter");
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testAndFilter(TestContext context) {
		// Filter for entities that are both hostile AND have low health
		IsHostileFilter hostileFilter = new IsHostileFilter();
		HealthThresholdFilter lowHealthFilter = new HealthThresholdFilter(0.5f, HealthThresholdFilter.Comparator.LESS_THAN);
		AndFilter andFilter = new AndFilter(List.of(hostileFilter, lowHealthFilter));

		LivingEntity source = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(0, 1, 0));
		ZombieEntity hostileLowHealth = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(1, 1, 0));
		ZombieEntity hostileHighHealth = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(2, 1, 0));
		VillagerEntity peacefulLowHealth = context.spawnEntity(EntityType.VILLAGER, new BlockPos(3, 1, 0));

		hostileLowHealth.setHealth(hostileLowHealth.getMaxHealth() * 0.25f);
		peacefulLowHealth.setHealth(peacefulLowHealth.getMaxHealth() * 0.25f);

		context.assertTrue(andFilter.test(source, hostileLowHealth), "Hostile low-health entity should pass AND filter");
		context.assertFalse(andFilter.test(source, hostileHighHealth), "Hostile high-health entity should not pass AND filter");
		context.assertFalse(andFilter.test(source, peacefulLowHealth), "Peaceful low-health entity should not pass AND filter");
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testOrFilter(TestContext context) {
		// Filter for entities that are either hostile OR have low health
		IsHostileFilter hostileFilter = new IsHostileFilter();
		HealthThresholdFilter lowHealthFilter = new HealthThresholdFilter(0.5f, HealthThresholdFilter.Comparator.LESS_THAN);
		OrFilter orFilter = new OrFilter(List.of(hostileFilter, lowHealthFilter));

		LivingEntity source = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(0, 1, 0));
		ZombieEntity hostileLowHealth = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(1, 1, 0));
		ZombieEntity hostileHighHealth = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(2, 1, 0));
		VillagerEntity peacefulLowHealth = context.spawnEntity(EntityType.VILLAGER, new BlockPos(3, 1, 0));
		VillagerEntity peacefulHighHealth = context.spawnEntity(EntityType.VILLAGER, new BlockPos(4, 1, 0));

		hostileLowHealth.setHealth(hostileLowHealth.getMaxHealth() * 0.25f);
		peacefulLowHealth.setHealth(peacefulLowHealth.getMaxHealth() * 0.25f);

		context.assertTrue(orFilter.test(source, hostileLowHealth), "Hostile low-health entity should pass OR filter");
		context.assertTrue(orFilter.test(source, hostileHighHealth), "Hostile high-health entity should pass OR filter");
		context.assertTrue(orFilter.test(source, peacefulLowHealth), "Peaceful low-health entity should pass OR filter");
		context.assertFalse(orFilter.test(source, peacefulHighHealth), "Peaceful high-health entity should not pass OR filter");
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testNotFilter(TestContext context) {
		IsHostileFilter hostileFilter = new IsHostileFilter();
		NotFilter notHostileFilter = new NotFilter(hostileFilter);

		LivingEntity source = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(0, 1, 0));
		ZombieEntity hostile = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(1, 1, 0));
		VillagerEntity peaceful = context.spawnEntity(EntityType.VILLAGER, new BlockPos(2, 1, 0));

		context.assertFalse(notHostileFilter.test(source, hostile), "Hostile entity should not pass NOT hostile filter");
		context.assertTrue(notHostileFilter.test(source, peaceful), "Peaceful entity should pass NOT hostile filter");
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testHasTagFilter(TestContext context) {
		// Test for undead tag (zombies and skeletons are undead)
		HasTagFilter undeadFilter = new HasTagFilter("minecraft:undead");

		LivingEntity source = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(0, 1, 0));
		LivingEntity zombie = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(1, 1, 0));
		LivingEntity skeleton = context.spawnEntity(EntityType.SKELETON, new BlockPos(2, 1, 0));
		LivingEntity villager = context.spawnEntity(EntityType.VILLAGER, new BlockPos(3, 1, 0));

		context.assertTrue(undeadFilter.test(source, zombie), "Zombie should have undead tag");
		context.assertTrue(undeadFilter.test(source, skeleton), "Skeleton should have undead tag");
		context.assertFalse(undeadFilter.test(source, villager), "Villager should not have undead tag");
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testEntityTypeFilter(TestContext context) {
		EntityTypeFilter zombieFilter = new EntityTypeFilter("minecraft:zombie");
		EntityTypeFilter villagerFilter = new EntityTypeFilter("minecraft:villager");

		LivingEntity source = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(0, 1, 0));
		LivingEntity zombie = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(1, 1, 0));
		LivingEntity skeleton = context.spawnEntity(EntityType.SKELETON, new BlockPos(2, 1, 0));
		LivingEntity villager = context.spawnEntity(EntityType.VILLAGER, new BlockPos(3, 1, 0));

		context.assertTrue(zombieFilter.test(source, zombie), "Should pass for zombie when filtering for zombies");
		context.assertFalse(zombieFilter.test(source, skeleton), "Should fail for skeleton when filtering for zombies");
		context.assertFalse(zombieFilter.test(source, villager), "Should fail for villager when filtering for zombies");

		context.assertTrue(villagerFilter.test(source, villager), "Should pass for villager when filtering for villagers");
		context.assertFalse(villagerFilter.test(source, zombie), "Should fail for zombie when filtering for villagers");
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testIsPlayerFilter(TestContext context) {
		IsPlayerFilter filter = new IsPlayerFilter();

		LivingEntity source = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(0, 1, 0));
		LivingEntity zombie = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(1, 1, 0));
		var player = context.createMockCreativeServerPlayerInWorld();

		context.assertFalse(filter.test(source, zombie), "Should fail for non-player entity");
		context.assertTrue(filter.test(source, player), "Should pass for player entity");
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testIsBurningFilter(TestContext context) {
		IsBurningFilter filter = new IsBurningFilter();

		LivingEntity source = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(0, 1, 0));
		LivingEntity notBurning = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(1, 1, 0));
		LivingEntity burning = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(2, 1, 0));

		burning.setOnFireFor(5);

		context.assertFalse(filter.test(source, notBurning), "Should fail for entity not on fire");
		context.assertTrue(filter.test(source, burning), "Should pass for burning entity");
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testIsInWaterFilter(TestContext context) {
		IsInWaterFilter filter = new IsInWaterFilter();

		// Place water at position
		BlockPos waterPos = new BlockPos(2, 1, 2);
		context.setBlockState(waterPos, net.minecraft.block.Blocks.WATER);

		LivingEntity source = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(0, 1, 0));
		LivingEntity notInWater = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(1, 1, 1));
		LivingEntity inWater = context.spawnEntity(EntityType.ZOMBIE, waterPos);

		context.assertFalse(filter.test(source, notInWater), "Should fail for entity not in water");
		context.assertTrue(filter.test(source, inWater), "Should pass for entity in water");
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testHasEffectFilter(TestContext context) {
		HasEffectFilter poisonFilter = new HasEffectFilter("minecraft:poison");

		LivingEntity source = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(0, 1, 0));
		LivingEntity withoutEffect = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(1, 1, 0));
		LivingEntity withEffect = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(2, 1, 0));

		// Apply poison effect
		withEffect.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(
				net.minecraft.entity.effect.StatusEffects.POISON, 100, 0));

		context.assertFalse(poisonFilter.test(source, withoutEffect), "Should fail for entity without poison effect");
		context.assertTrue(poisonFilter.test(source, withEffect), "Should pass for entity with poison effect");
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testRandomChanceFilter(TestContext context) {
		// Test with 100% chance - should always pass
		RandomChanceFilter alwaysFilter = new RandomChanceFilter(1.0f);
		// Test with 0% chance - should never pass
		RandomChanceFilter neverFilter = new RandomChanceFilter(0.0f);

		LivingEntity source = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(0, 1, 0));
		LivingEntity target = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(1, 1, 0));

		context.assertTrue(alwaysFilter.test(source, target), "100% chance filter should always pass");
		context.assertFalse(neverFilter.test(source, target), "0% chance filter should never pass");
		context.complete();
	}
}
