package com.sigmundgranaas.forgero.gametest;

import java.util.List;
import java.util.Optional;

import com.sigmundgranaas.forgero.common.runtime.DynamicContext;
import com.sigmundgranaas.forgero.common.runtime.MinecraftContextKeys;
import com.sigmundgranaas.forgero.predicate.minecraft.standalone.MoonPhasePredicate;
import com.sigmundgranaas.forgero.predicate.minecraft.standalone.TimeOfDayPredicate;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

/**
 * Phase 4: time-of-day and moon-phase identity conditions.
 */
public class IdentityConditionGametest {
	private static final String EMPTY_STRUCTURE = FabricGameTest.EMPTY_STRUCTURE;

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void testTimeOfDayPhase(TestContext context) {
		ServerWorld world = context.getWorld();
		DynamicContext ctx = new DynamicContext.Builder().put(MinecraftContextKeys.WORLD, world).build();

		world.setTimeOfDay(1000); // morning
		context.assertTrue(new TimeOfDayPredicate(Optional.of("day"), Optional.empty(), Optional.empty()).test(ctx),
				"1000 ticks should be daytime");
		context.assertFalse(new TimeOfDayPredicate(Optional.of("night"), Optional.empty(), Optional.empty()).test(ctx),
				"1000 ticks should not be night");

		world.setTimeOfDay(15000); // night
		context.assertTrue(new TimeOfDayPredicate(Optional.of("night"), Optional.empty(), Optional.empty()).test(ctx),
				"15000 ticks should be night");
		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void testMoonPhase(TestContext context) {
		ServerWorld world = context.getWorld();
		DynamicContext ctx = new DynamicContext.Builder().put(MinecraftContextKeys.WORLD, world).build();

		world.setTimeOfDay(15000); // day 0 night -> moon phase 0 (full)
		context.assertTrue(new MoonPhasePredicate(List.of(0)).test(ctx), "Day 0 should be moon phase 0");
		context.assertFalse(new MoonPhasePredicate(List.of(1, 2)).test(ctx), "Day 0 is not phase 1 or 2");
		context.complete();
	}
}
