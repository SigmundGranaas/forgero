package com.sigmundgranaas.forgero.gametest;

import com.sigmundgranaas.forgero.common.runtime.DynamicContext;
import com.sigmundgranaas.forgero.predicate.minecraft.MinecraftContextKeys;
import com.sigmundgranaas.forgero.predicate.minecraft.entity.RelationalPredicate;
import com.sigmundgranaas.forgero.predicate.minecraft.util.NumericPredicate;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

import java.util.Optional;

public class RelationalPredicateGametest {
	private static final String EMPTY_STRUCTURE = FabricGameTest.EMPTY_STRUCTURE;

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void testDistanceToMatch(TestContext context) {
		NumericPredicate distance = new NumericPredicate(Optional.empty(), Optional.of(5.0), Optional.empty());
		RelationalPredicate predicate = new RelationalPredicate(Optional.of(distance));

		Entity self = context.spawnEntity(EntityType.PIG, new BlockPos(0, 1, 0));
		Entity target = context.spawnEntity(EntityType.PIG, new BlockPos(3, 1, 0));

		DynamicContext dynamicContext = new DynamicContext.Builder()
				.put(MinecraftContextKeys.ENTITY, self)
				.put(MinecraftContextKeys.TARGET_ENTITY, target)
				.build();

		context.assertTrue(predicate.test(dynamicContext), "Predicate should match when entities are close");
		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void testDistanceToMismatch(TestContext context) {
		NumericPredicate distance = new NumericPredicate(Optional.of(10.0), Optional.empty(), Optional.empty());
		RelationalPredicate predicate = new RelationalPredicate(Optional.of(distance));

		Entity self = context.spawnEntity(EntityType.PIG, new BlockPos(0, 1, 0));
		Entity target = context.spawnEntity(EntityType.PIG, new BlockPos(3, 1, 0));

		DynamicContext dynamicContext = new DynamicContext.Builder()
				.put(MinecraftContextKeys.ENTITY, self)
				.put(MinecraftContextKeys.TARGET_ENTITY, target)
				.build();

		context.assertFalse(predicate.test(dynamicContext), "Predicate should not match when entities are too close");
		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void testDistanceMissingContext(TestContext context) {
		NumericPredicate distance = new NumericPredicate(Optional.of(10.0), Optional.empty(), Optional.empty());
		RelationalPredicate predicate = new RelationalPredicate(Optional.of(distance));
		Entity self = context.spawnEntity(EntityType.PIG, new BlockPos(0, 1, 0));

		// Missing target
		DynamicContext missingTargetCtx = new DynamicContext.Builder()
				.put(MinecraftContextKeys.ENTITY, self)
				.build();
		context.assertFalse(predicate.test(missingTargetCtx), "Predicate should fail without target entity");

		// Missing self
		DynamicContext missingSelfCtx = new DynamicContext.Builder()
				.put(MinecraftContextKeys.TARGET_ENTITY, self)
				.build();

		context.assertFalse(predicate.test(missingSelfCtx), "Predicate should fail without self entity");

		context.complete();
	}
}
