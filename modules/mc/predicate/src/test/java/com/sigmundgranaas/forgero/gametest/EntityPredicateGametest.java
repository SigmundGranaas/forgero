package com.sigmundgranaas.forgero.gametest;
import com.sigmundgranaas.forgero.common.runtime.MinecraftContextKeys;

import com.sigmundgranaas.forgero.common.runtime.DynamicContext;
import com.sigmundgranaas.forgero.predicate.minecraft.entity.EntityFlagPredicate;
import com.sigmundgranaas.forgero.predicate.minecraft.entity.EntityPredicate;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

import java.util.Optional;

public class EntityPredicateGametest {
	private static final String EMPTY_STRUCTURE = FabricGameTest.EMPTY_STRUCTURE;

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void testEntityTypeMatch(TestContext context) {
		EntityPredicate predicate = new EntityPredicate(Optional.empty(), Optional.of(EntityType.ZOMBIE), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
		Entity zombie = context.spawnEntity(EntityType.ZOMBIE, BlockPos.ORIGIN);

		DynamicContext dynamicContext = new DynamicContext.Builder()
				.put(MinecraftContextKeys.ENTITY, zombie)
				.build();

		context.assertTrue(predicate.test(dynamicContext), "Predicate should match zombie type");

		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void testTargetEntity(TestContext context) {
		EntityPredicate predicate = new EntityPredicate(Optional.of(EntityPredicate.Target.TARGET_ENTITY), Optional.of(EntityType.PIG), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());

		Entity zombie = context.spawnEntity(EntityType.ZOMBIE, BlockPos.ORIGIN);
		Entity pig = context.spawnEntity(EntityType.PIG, BlockPos.ORIGIN.east());

		DynamicContext dynamicContext = new DynamicContext.Builder()
				.put(MinecraftContextKeys.ENTITY, zombie)
				.put(MinecraftContextKeys.TARGET_ENTITY, pig)
				.build();

		context.assertTrue(predicate.test(dynamicContext), "Predicate should match target entity (pig)");
		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void testSelfEntity(TestContext context) {
		EntityPredicate predicate = new EntityPredicate(Optional.of(EntityPredicate.Target.SELF), Optional.of(EntityType.ZOMBIE), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());

		Entity zombie = context.spawnEntity(EntityType.ZOMBIE, BlockPos.ORIGIN);
		Entity pig = context.spawnEntity(EntityType.PIG, BlockPos.ORIGIN.east());

		DynamicContext dynamicContext = new DynamicContext.Builder()
				.put(MinecraftContextKeys.ENTITY, zombie)
				.put(MinecraftContextKeys.TARGET_ENTITY, pig)
				.build();

		context.assertTrue(predicate.test(dynamicContext), "Predicate should match self entity (zombie)");
		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void testCombinedPredicateMatch(TestContext context) {
		EntityFlagPredicate flags = new EntityFlagPredicate(Optional.empty(), Optional.of(true), Optional.empty(), Optional.empty(), Optional.empty());
		EntityPredicate predicate = new EntityPredicate(Optional.empty(), Optional.of(EntityType.ZOMBIE), Optional.of(flags), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());

		Entity zombie = context.spawnEntity(EntityType.ZOMBIE, BlockPos.ORIGIN);
		zombie.setFireTicks(100);

		DynamicContext dynamicContext = new DynamicContext.Builder()
				.put(MinecraftContextKeys.ENTITY, zombie)
				.build();
		context.assertTrue(predicate.test(dynamicContext), "Combined predicate (type and flag) should match");

		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void testMissingEntityInContext(TestContext context) {
		EntityPredicate predicate = new EntityPredicate(Optional.empty(), Optional.of(EntityType.ZOMBIE), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());

		DynamicContext emptyContext = new DynamicContext.Builder()
				.build();

		context.assertFalse(predicate.test(emptyContext), "Predicate should fail with empty context");
		context.complete();
	}
}
