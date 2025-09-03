package com.sigmundgranaas.forgero.gametest;

import com.sigmundgranaas.forgero.predicate.minecraft.entity.EntityFlagPredicate;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

import java.util.Optional;

public class EntityFlagPredicateGametest {
	private static final String EMPTY_STRUCTURE = FabricGameTest.EMPTY_STRUCTURE;

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void testIsSneakingTrue(TestContext context) {
		EntityFlagPredicate predicate = new EntityFlagPredicate(Optional.of(true), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
		LivingEntity entity = context.spawnEntity(EntityType.ARMOR_STAND, BlockPos.ORIGIN);

		entity.setSneaking(true);
		context.assertTrue(predicate.test(entity), "Predicate should be true when entity is sneaking");

		entity.setSneaking(false);
		context.assertFalse(predicate.test(entity), "Predicate should be false when entity is not sneaking");

		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void testIsOnFireFalse(TestContext context) {
		EntityFlagPredicate predicate = new EntityFlagPredicate(Optional.empty(), Optional.of(false), Optional.empty(), Optional.empty(), Optional.empty());
		LivingEntity entity = context.spawnEntity(EntityType.ARMOR_STAND, BlockPos.ORIGIN);

		entity.setFireTicks(0);
		context.assertTrue(predicate.test(entity), "Predicate should be true when entity is not on fire");

		entity.setFireTicks(100);
		context.assertFalse(predicate.test(entity), "Predicate should be false when entity is on fire");

		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void testMultipleEntityFlagsMatch(TestContext context) {
		EntityFlagPredicate predicate = new EntityFlagPredicate(Optional.of(true), Optional.empty(), Optional.of(true), Optional.of(true), Optional.empty());
		LivingEntity entity = context.spawnEntity(EntityType.ARMOR_STAND, new BlockPos(0, 1, 0));

		entity.setSneaking(true);
		entity.setSprinting(true);

		// FIX: Wait a few ticks for the entity to fall and update its isOnGround status.
		context.waitAndRun(5, () -> {
			context.assertTrue(predicate.test(entity), "Predicate should match with multiple correct flags");
			context.complete();
		});
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void testIsOnGround(TestContext context) {
		EntityFlagPredicate predicate = new EntityFlagPredicate(Optional.empty(), Optional.empty(), Optional.of(true), Optional.empty(), Optional.empty());
		LivingEntity entity = context.spawnEntity(EntityType.PIG, new BlockPos(0, 1, 0));

		context.waitAndRun(5, () -> {
			context.assertTrue(entity.isOnGround(), "Entity should be on the ground");
			context.assertTrue(predicate.test(entity), "Predicate for is_on_ground should be true");
			context.complete();
		});
	}
}
