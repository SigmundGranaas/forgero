package com.sigmundgranaas.forgero.properties.gametest;

import com.sigmundgranaas.forgero.common.runtime.DynamicContext;
import com.sigmundgranaas.forgero.common.runtime.MinecraftContextKeys;
import com.sigmundgranaas.forgero.predicate.minecraft.standalone.BackstabPredicate;
import com.sigmundgranaas.forgero.predicate.minecraft.standalone.CrowdCountPredicate;
import com.sigmundgranaas.forgero.properties.minecraft.entityfilter.FacingAwayFilter;
import com.sigmundgranaas.forgero.properties.minecraft.entityfilter.IsAirborneFilter;
import com.sigmundgranaas.forgero.properties.minecraft.entityfilter.LineOfSightFilter;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

import java.util.Optional;

/**
 * Phase 2: positional / skill combat filters and conditions.
 */
public class PositionalCombatGametest {

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testIsAirborneFilter(TestContext context) {
		LivingEntity entity = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(1, 2, 1));
		IsAirborneFilter filter = IsAirborneFilter.INSTANCE;

		entity.setOnGround(true);
		context.assertFalse(filter.test(null, entity), "On-ground entity should not be airborne");
		entity.setOnGround(false);
		context.assertTrue(filter.test(null, entity), "Off-ground entity should be airborne");
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testFacingAwayFilter(TestContext context) {
		// Candidate faces east (+X).
		LivingEntity candidate = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(5, 1, 5));
		candidate.setYaw(-90);
		candidate.setHeadYaw(-90);

		LivingEntity behind = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(2, 1, 5)); // west, behind
		LivingEntity inFront = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(8, 1, 5)); // east, in front

		FacingAwayFilter filter = new FacingAwayFilter(90.0f);
		context.assertTrue(filter.test(behind, candidate), "Candidate should count as facing away from a source behind it");
		context.assertFalse(filter.test(inFront, candidate), "Candidate facing the source should not count as facing away");
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testLineOfSightFilter(TestContext context) {
		LivingEntity source = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(1, 2, 1));
		LivingEntity target = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(5, 2, 1));
		LineOfSightFilter filter = LineOfSightFilter.INSTANCE;

		context.assertTrue(filter.test(source, target), "Clear line of sight should pass");

		// Build a wall between them, tall enough to cross eye height.
		context.setBlockState(new BlockPos(3, 2, 1), Blocks.STONE.getDefaultState());
		context.setBlockState(new BlockPos(3, 3, 1), Blocks.STONE.getDefaultState());
		context.setBlockState(new BlockPos(3, 4, 1), Blocks.STONE.getDefaultState());
		context.assertFalse(filter.test(source, target), "A wall should block line of sight");
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testBackstabCondition(TestContext context) {
		// Target faces east (+X).
		LivingEntity target = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(5, 1, 5));
		target.setYaw(-90);
		target.setHeadYaw(-90);

		LivingEntity behind = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(2, 1, 5)); // west
		LivingEntity inFront = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(8, 1, 5)); // east

		BackstabPredicate condition = new BackstabPredicate(90.0f);

		DynamicContext rear = new DynamicContext.Builder()
				.put(MinecraftContextKeys.SOURCE_ENTITY, behind)
				.put(MinecraftContextKeys.TARGET_ENTITY, target).build();
		DynamicContext front = new DynamicContext.Builder()
				.put(MinecraftContextKeys.SOURCE_ENTITY, inFront)
				.put(MinecraftContextKeys.TARGET_ENTITY, target).build();

		context.assertTrue(condition.test(rear), "Attacking from behind should be a backstab");
		context.assertFalse(condition.test(front), "Attacking from the front should not be a backstab");
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testCrowdCountCondition(TestContext context) {
		LivingEntity source = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(5, 1, 5));
		context.spawnEntity(EntityType.ZOMBIE, new BlockPos(6, 1, 5));
		context.spawnEntity(EntityType.ZOMBIE, new BlockPos(4, 1, 5));
		context.spawnEntity(EntityType.ZOMBIE, new BlockPos(5, 1, 6));

		DynamicContext ctx = new DynamicContext.Builder()
				.put(MinecraftContextKeys.SOURCE_ENTITY, source).build();

		// At least 3 others within radius 4 (source excluded). Robust to extra shared-world entities,
		// which only increase the count.
		context.assertTrue(new CrowdCountPredicate(4.0, 3, Optional.empty()).test(ctx),
				"Should detect a crowd of >= 3");
		context.assertFalse(new CrowdCountPredicate(4.0, 50, Optional.empty()).test(ctx),
				"Should not report 50+ nearby entities");
		context.complete();
	}
}
