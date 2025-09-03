package com.sigmundgranaas.forgero.gametest;

import com.sigmundgranaas.forgero.predicate.minecraft.util.DimensionPredicate;
import com.sigmundgranaas.forgero.predicate.minecraft.util.LightPredicate;
import com.sigmundgranaas.forgero.predicate.minecraft.util.LocationPredicate;
import com.sigmundgranaas.forgero.predicate.minecraft.util.NumericPredicate;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;
import java.util.Optional;

public class LocationPredicateGametest {
	private static final String EMPTY_STRUCTURE = FabricGameTest.EMPTY_STRUCTURE;

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void testDimensionMatch(TestContext context) {
		DimensionPredicate dimension = new DimensionPredicate(List.of(World.OVERWORLD.getValue()));
		LocationPredicate predicate = new LocationPredicate(Optional.empty(), Optional.of(dimension), Optional.empty(), Optional.empty(), Optional.empty());

		context.assertTrue(predicate.test(context.getWorld(), BlockPos.ORIGIN), "Predicate should match overworld dimension");
		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void testDimensionMismatch(TestContext context) {
		DimensionPredicate dimension = new DimensionPredicate(List.of(World.NETHER.getValue()));
		LocationPredicate predicate = new LocationPredicate(Optional.empty(), Optional.of(dimension), Optional.empty(), Optional.empty(), Optional.empty());

		context.assertFalse(predicate.test(context.getWorld(), BlockPos.ORIGIN), "Predicate should not match nether dimension");
		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void testLightLevelMatch(TestContext context) {
		NumericPredicate lightLevel = new NumericPredicate(Optional.of(10.0), Optional.empty(), Optional.empty());
		LightPredicate light = new LightPredicate(Optional.of(lightLevel), Optional.empty());
		LocationPredicate predicate = new LocationPredicate(Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(light), Optional.empty());
		BlockPos testPos = new BlockPos(0, 1, 0);

		context.setBlockState(testPos.up(3), Blocks.GLOWSTONE);

		context.waitAndRun(10, () -> {
			context.assertTrue(predicate.test(context.getWorld(), context.getAbsolutePos(testPos)), "Glowstone should provide enough block light");
			context.complete();
		});
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void testCanSeeSky(TestContext context) {
		LocationPredicate canSeeSkyPredicate = new LocationPredicate(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(true));
		LocationPredicate cannotSeeSkyPredicate = new LocationPredicate(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(false));
		BlockPos testPos = new BlockPos(0, 1, 0);

		context.assertTrue(canSeeSkyPredicate.test(context.getWorld(), context.getAbsolutePos(testPos)), "Position should be able to see sky in empty structure");

		context.setBlockState(testPos.up(2), Blocks.STONE);

		context.waitAndRun(5, () -> {
			BlockPos absolutePos = context.getAbsolutePos(testPos);
			context.assertFalse(canSeeSkyPredicate.test(context.getWorld(), absolutePos), "Stone block should obstruct sky view");
			context.assertTrue(cannotSeeSkyPredicate.test(context.getWorld(), absolutePos), "Should now be considered unable to see sky");
			context.complete();
		});
	}

	@GameTest(templateName = EMPTY_STRUCTURE)
	public void testCombinedLocationPredicateMatch(TestContext context) {
		DimensionPredicate dimension = new DimensionPredicate(List.of(World.OVERWORLD.getValue()));
		LightPredicate light = new LightPredicate(Optional.empty(), Optional.of(new NumericPredicate(Optional.empty(), Optional.of(5.0), Optional.empty())));
		LocationPredicate predicate = new LocationPredicate(Optional.empty(), Optional.of(dimension), Optional.empty(), Optional.of(light), Optional.of(false));

		// Test position is inside the box we are about to build
		BlockPos testPos = new BlockPos(2, 2, 2);

		// Build a 3x3x3 hollow box to create a guaranteed dark and covered area.
		for (int x = 1; x <= 3; x++) {
			for (int y = 1; y <= 3; y++) {
				for (int z = 1; z <= 3; z++) {
					// If the position is on the outer shell of the 3x3x3 cube, place a block.
					if (x == 1 || x == 3 || y == 1 || y == 3 || z == 1 || z == 3) {
						context.setBlockState(new BlockPos(x, y, z), Blocks.BLACK_WOOL);
					}
				}
			}
		}

		// Wait for light levels to update
		context.waitAndRun(20, () -> {
			context.assertTrue(predicate.test(context.getWorld(), context.getAbsolutePos(testPos)), "Predicate should match in dark, covered, overworld location");
			context.complete();
		});
	}
}
