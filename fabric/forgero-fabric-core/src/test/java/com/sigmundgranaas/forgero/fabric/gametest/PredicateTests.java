package com.sigmundgranaas.forgero.fabric.gametest;

import static com.sigmundgranaas.forgero.minecraft.common.match.MinecraftContextKeys.BLOCK_TARGET;
import static com.sigmundgranaas.forgero.minecraft.common.match.MinecraftContextKeys.WORLD;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.minecraft.common.match.predicate.RandomPredicate;

import net.minecraft.test.GameTest;
import net.minecraft.test.GameTestException;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;

public class PredicateTests {

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "predicateTests1")
	public void testRandomPredicateConsistency(TestContext context) {
		World world = context.getWorld();
		BlockPos testPos = new BlockPos(1, 2, 3);

		RandomPredicate predicate = new RandomPredicate(0.5f, 1200,
				Arrays.asList(RandomPredicate.SeedSource.BLOCK_POS, RandomPredicate.SeedSource.WORLD_TIME));

		MatchContext matchContext = new MatchContext().put(WORLD, world).put(BLOCK_TARGET, testPos);

		// Run the predicate multiple times and store results
		Map<Long, Boolean> results = new HashMap<>();
		for (int i = 0; i < 10; i++) {
			long worldTime = world.getTime();
			boolean result = predicate.test(null, matchContext);
			results.put(worldTime, result);
		}

		context.waitAndRun(100, () -> {
			// Verify consistency
			for (Map.Entry<Long, Boolean> entry : results.entrySet()) {
				long worldTime = entry.getKey();
				boolean expectedResult = entry.getValue();

				boolean actualResult = predicate.test(null, matchContext);

				if (expectedResult != actualResult) {
					throw new GameTestException("Inconsistent result for world time " + worldTime);
				}
			}
		});

		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "predicateTests2")
	public void testRandomPredicateDistribution(TestContext context) {
		World world = context.getWorld();
		BlockPos testPos = new BlockPos(1, 2, 3);

		RandomPredicate predicate = new RandomPredicate(0.5f, 1,
				Arrays.asList(RandomPredicate.SeedSource.BLOCK_POS, RandomPredicate.SeedSource.WORLD_TIME));

		MatchContext matchContext = new MatchContext().put(WORLD, world).put(BLOCK_TARGET, testPos);

		int trueCount = 0;
		int totalTests = 1000;

		long time = context.getWorld().getTime();
		for (int i = 0; i < totalTests; i++) {
			context.setTime((int) (time + i));

			context.getWorld().tick(() -> true);

			if (predicate.test(null, matchContext)) {
				trueCount += 1;
			}
		}

		context.setTime((int) time);

		context.getWorld().tick(() -> true);

		double ratio = (double) trueCount / totalTests;
		if (ratio < 0.4 || ratio > 0.6) {
			throw new GameTestException("Distribution not within expected range. Actual ratio: " + ratio);
		}

		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "predicateTests3")
	public void testDifferentQuantizationRanges(TestContext context) {
		World world = context.getWorld();
		BlockPos testPos = new BlockPos(1, 2, 3);

		long time = context.getWorld().getTime();

		int[] quantizationRanges = {1, 20, 100};
		for (int quantization : quantizationRanges) {
			RandomPredicate predicate = new RandomPredicate(0.5f, quantization,
					Arrays.asList(RandomPredicate.SeedSource.BLOCK_POS, RandomPredicate.SeedSource.WORLD_TIME));

			MatchContext matchContext = new MatchContext().put(WORLD, world).put(BLOCK_TARGET, testPos);

			int trueCount = 0;
			int totalTests = 1000;

			for (int i = 0; i < totalTests; i++) {
				context.setTime(i * quantization);
				context.getWorld().tick(() -> true);

				if (predicate.test(null, matchContext)) {
					trueCount++;
				}
			}

			double ratio = (double) trueCount / totalTests;
			if (ratio < 0.4 || ratio > 0.6) {
				throw new GameTestException("Distribution not within expected range for quantization " + quantization + ". Actual ratio: " + ratio);
			}
		}

		context.setTime((int) time);
		context.getWorld().tick(() -> true);
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "predicateTests4")
	public void testConsistencyWithinQuantization(TestContext context) {
		World world = context.getWorld();
		BlockPos testPos = new BlockPos(1, 2, 3);
		int quantization = 100;
		RandomPredicate predicate = new RandomPredicate(0.5f, quantization,
				Arrays.asList(RandomPredicate.SeedSource.BLOCK_POS, RandomPredicate.SeedSource.WORLD_TIME));

		MatchContext matchContext = new MatchContext().put(WORLD, world).put(BLOCK_TARGET, testPos);

		long startTime = world.getTime();
		long currentQuantizedTime = startTime / quantization;
		Boolean lastResult = null;

		for (int i = 0; i < 1000; i++) {  // Run for a longer time to catch more transitions
			context.getWorld().tick(() -> false);
			long currentTime = world.getTime();
			long newQuantizedTime = currentTime / quantization;

			boolean currentResult = predicate.test(null, matchContext);

			if (newQuantizedTime == currentQuantizedTime) {
				// We're in the same quantized bracket
				if (lastResult != null && currentResult != lastResult) {
					throw new GameTestException("Inconsistent result within quantization range. Time: " + currentTime);
				}
			} else {
				// We've transitioned to a new quantized bracket
				currentQuantizedTime = newQuantizedTime;
				lastResult = null;  // Reset the last result as we're in a new bracket
			}

			lastResult = currentResult;
		}

		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "predicateTests5")
	public void testBlockPositionSeedChange(TestContext context) {
		World world = context.getWorld();

		RandomPredicate predicate = new RandomPredicate(0.5f, 0,
				Arrays.asList(RandomPredicate.SeedSource.BLOCK_POS));

		MatchContext matchContext = new MatchContext().put(WORLD, world);

		BlockPos[] testPositions = {
				new BlockPos(1, 1, 1),
				new BlockPos(2, 2, 2),
				new BlockPos(3, 3, 3),
				new BlockPos(-1, -1, -1),
				new BlockPos(10, 20, 30)
		};

		// Run the test multiple times to catch different time quantizations
		for (int run = 0; run < 5; run++) {
			Map<BlockPos, Boolean> results = new HashMap<>();
			context.getWorld().tick(() -> true);

			for (BlockPos pos : testPositions) {
				results.put(pos, predicate.test(null, matchContext.put(BLOCK_TARGET, pos)));

			}

			// Only perform checks if we completed all positions within the same time quantization
			if (results.size() == testPositions.length) {
				// Verify that changing block position changes the result
				long uniqueResults = results.values().stream().distinct().count();
				if (uniqueResults < 2) {
					throw new GameTestException("Block position changes did not affect the result as expected.");
				}

				// Verify consistency for the same block position
				for (BlockPos pos : testPositions) {
					boolean newResult = predicate.test(null, matchContext.put(BLOCK_TARGET, pos));
					if (newResult != results.get(pos)) {
						throw new GameTestException("Inconsistent result for the same block position: " + pos);
					}
				}
			}
		}

		context.complete();
	}
}
