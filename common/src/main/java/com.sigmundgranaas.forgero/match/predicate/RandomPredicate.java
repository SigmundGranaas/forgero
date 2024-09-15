package com.sigmundgranaas.forgero.match.predicate;

import static com.sigmundgranaas.forgero.match.MinecraftContextKeys.*;
import static com.sigmundgranaas.forgero.match.predicate.RandomPredicate.SeedComputations.combineSeedSources;
import static com.sigmundgranaas.forgero.match.predicate.RandomPredicate.SeedComputations.computeWorldTimeSeed;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;


/**
 * Represents a predicate that evaluates to true based on a random chance.
 * The randomness can be controlled by specifying one or more seed sources in the JSON configuration.
 * The available seed sources are BLOCKPOS, TARGET_ENTITY, SOURCE_ENTITY, WORLD_TIME, and NONE.
 * The seeds are used to generate random number that are consistent between server and client.
 *
 * <p>Example JSON configurations:</p>
 *
 * <p>Random chance based on block position:</p>
 * <pre>
 * {
 *   "type": "forgero:random",
 *   "value": 0.5,
 *   "seed": ["block_pos"]
 * }
 * </pre>
 *
 * <p>Random chance based on a combination of world time and the target entity:</p>
 * <pre>
 * {
 *   "type": "forgero:random",
 *   "value": 0.3,
 *   "seed": ["world_time", "target_entity"]
 * }
 * </pre>
 *
 * <p>Random chance based on quantized world time (changes every 1200 ticks, which is 1 minute):</p>
 * <pre>
 * {
 *   "type": "forgero:random",
 *   "value": 0.3,
 *   "seed": ["world_time"],
 *   "world_time_quantization": 1200
 * }
 * </pre>
 *
 *
 * <p>Completely random chance (no seed source):</p>
 * <pre>
 * {
 *   "type": "forgero:random",
 *   "value": 0.7,
 *   "seed": ["none"]
 * }
 * </pre>
 *
 * <p>Random chance with no seed sources specified (defaults to completely random):</p>
 * <pre>
 * {
 *   "type": "forgero:random",
 *   "value": 0.7
 * }
 * </pre>
 *
 * <p>Real world example using a feature which will have a 50% chance of breaking a block:</p>
 * <pre>
 *  {
 * 		 "type": "minecraft:block_breaking",
 * 		 "selector": {
 * 		     "type": "forgero:single"
 *         },
 * 		 "speed": "forgero:instant",
 * 		 "predicate": {
 * 		     "type": "forgero:random",
 * 		     "seed": ["world_time", "block_pos"],
 * 		     "value": 0.5
 *         },
 * 		 "title": "feature.forgero.random_insta_break.title",
 * 		 "description": "feature.forgero.random_insta_break.description"
 *   }
 * </pre>
 */
public record RandomPredicate(float value, int worldTimeQuantization,
                              List<SeedSource> seedSources) implements Matchable {
	public static String ID = "forgero:random";

	public static String simpleComment = """
			Random chance with no seed sources specified (defaults to completely random)
			In this case, 50%
			""";
	public static String SIMPLE_RANDOM_EXAMPLE = """
			{
				"type": "forgero:random",
				"value": 0.5
			}
			""";

	public static String seededComment = """
			Random chance with seeds.
			This means that when the input seeds (world time and block pos) are equal, this function will always give the same value.
			This can be used in cases where we need the random function to give the same result on the server and the client
			If block breaking conditions are used, this will make sure that at the exact same time of day, and for the same block pos, this function would give the same result.
			""";
	public static String SEEDED_RANDOM_EXAMPLE = """
			{
				"type": "forgero:random",
				"seed": ["world_time", "block_pos"],
				"value": 0.5
			}
			""";

	public static String quantComment = """
			Using quants.
			This breaks down the world time aspect into quants.
			This means that as long as the world time is within a given chunk of time, we should provide the same result.
			World time between server and client might not match exactly (I think?) so we need to give it some slack.
			Quants are defined as ticks, so in this case, it will yield the same results in 1 second intervals for the same block pos.
			If the mining operation is taking longer than 1 second, you should definitely increase this limit.
			You can also increase the quant size to something bigger if you want something to only be changed a couple of times a day.
			""";
	public static String SEEDED_QUANT_RANDOM_EXAMPLE = """
			{
				"type": "forgero:random",
				"seed": ["world_time", "block_pos"],
				"world_time_quantization": 20,
				"value": 0.5
			}
			""";

	// A fixed seed to mix with context-based seeds
	// This improves the "randomness" of the predicate when using time based seeds.
	private static final long FIXED_SEED = 0x12345678ABCDL;

	public static Codec<SeedSource> SEED_SOURCE_PREDICATE = Codec.STRING.xmap(RandomPredicate::from, SeedSource::toString);

	public static Codec<RandomPredicate> CODEC = RecordCodecBuilder
			.create(instance -> instance.group(
							Codec.FLOAT.fieldOf("value").forGetter(RandomPredicate::value),
							Codec.INT.optionalFieldOf("world_time_quantization", 0).forGetter(RandomPredicate::worldTimeQuantization),
							Codec.list(SEED_SOURCE_PREDICATE).optionalFieldOf("seed", Collections.emptyList()).forGetter(RandomPredicate::seedSources)
					)
					.apply(instance, RandomPredicate::factory));


	public static RandomPredicate factory(float value, int worldTimeQuantization, List<SeedSource> seedSources) {
		if (seedSources.isEmpty() && worldTimeQuantization != 0) {
			throw new IllegalArgumentException("World time quantization cannot be set to a number if there is no seed sources!");
		}
		return new RandomPredicate(value, worldTimeQuantization, seedSources);
	}


	public static SeedSource from(String source) {
		return SeedSource.valueOf(source.toUpperCase());
	}


	@Override
	public boolean test(Matchable match, MatchContext context) {
		Random random;
		if (this.seedSources.isEmpty()) {
			random = new Random();
		} else {
			long seed = generateSeed(context);
			random = new Random(seed);
		}

		return random.nextFloat() < value;
	}

	private long generateSeed(MatchContext context) {
		Long[] seeds = seedSources.stream()
				.map(source -> toLong(source, context))
				.toArray(Long[]::new);

		return combineSeedSources(seeds);
	}

	private long toLong(SeedSource source, MatchContext context) {
		return switch (source) {
			case BLOCK_POS -> context.get(BLOCK_TARGET).map(BlockPos::asLong).orElse(0L);
			case TARGET_ENTITY -> context.get(ENTITY_TARGET).map(Entity::getId).orElse(0);
			case SOURCE_ENTITY -> context.get(ENTITY).map(Entity::getId).orElse(0);
			case WORLD_TIME -> getWorldTimeSeed(context);
			case NONE -> 1L;
		};
	}

	private long getWorldTimeSeed(MatchContext context) {
		long worldTime = context.get(WORLD).map(World::getTime).orElse(0L);
		return computeWorldTimeSeed(worldTime, worldTimeQuantization);
	}

	@Override
	public boolean isDynamic() {
		return true;
	}

	public enum SeedSource {
		BLOCK_POS, TARGET_ENTITY, SOURCE_ENTITY, WORLD_TIME, NONE
	}

	public static class SeedComputations {
		public static long combineSeedSources(Long... seedSources) {
			long combinedSeed = FIXED_SEED;
			for (long sourceSeed : seedSources) {
				combinedSeed = combineSeed(combinedSeed, sourceSeed);
			}
			return combinedSeed;
		}

		public static long combineSeedSources(long... seedSources) {
			long combinedSeed = FIXED_SEED;
			for (long sourceSeed : seedSources) {
				combinedSeed = combineSeed(combinedSeed, sourceSeed);
			}
			return combinedSeed;
		}

		/**
		 * Combines the current seed with a new source seed.
		 * This method uses bit rotation and XOR for effective mixing.
		 *
		 * @param currentSeed The current combined seed
		 * @param sourceSeed  A new seed to be combined
		 * @return The result of combining the two seeds
		 */
		public static long combineSeed(long currentSeed, long sourceSeed) {
			// Rotate and XOR combination seems to be the one to provide the most consistent values, especially when using world time.
			return rotateAndXOR(currentSeed, sourceSeed);
		}

		/**
		 * Generates a seed value based on the world time, optionally applying quantization.
		 * We are using Sine functions to make sure that even small changes in values will provide significant changes in the outcomes.
		 * This is cycling, which means that the pattern will repeat once the computed angle goes above 360 degrees.
		 *
		 * @param worldTime    The current world time
		 * @param quantization The time quantization factor
		 * @return A seed value based on the world time
		 */
		public static long computeWorldTimeSeed(long worldTime, int quantization) {
			// Example 1: worldTime = 1234, quantization = 100
			// Example 2: worldTime = 5678, quantization = 0
			// Example 3: worldTime = 9876, quantization = 50

			// Step 1: Apply quantization if it's greater than 0
			if (quantization > 0) {
				worldTime = worldTime / quantization;
			}
			// Example 1: worldTime = 1234 / 100 = 12
			// Example 2: worldTime remains 5678 (no quantization)
			// Example 3: worldTime = 9876 / 50 = 197

			// Step 2: Convert the (possibly quantized) world time to an angle in radians
			// The modulo 360 ensures we stay within a full circle (360 degrees)
			// Multiplying by (Math.PI / 180) converts degrees to radians
			double angle = (worldTime % 360) * (Math.PI / 180);
			// Example 1: angle = (12 % 360) * (π/180) ≈ 0.209 radians
			// Example 2: angle = (5678 % 360) * (π/180) ≈ 1.571 radians
			// Example 3: angle = (197 % 360) * (π/180) ≈ 3.439 radians

			// Step 3: Use sine function to generate a value between -1 and 1,
			// then scale it to the full range of long values
			return (long) (Math.sin(angle) * Long.MAX_VALUE);
			// Example 1: sin(0.209) * Long.MAX_VALUE ≈ 1,876,474,783,463,180,930
			// Example 2: sin(1.571) * Long.MAX_VALUE ≈ 9,223,372,036,854,775,807 (MAX_VALUE)
			// Example 3: sin(3.439) * Long.MAX_VALUE ≈ -9,024,790,377,511,014,015
		}

		public static long simpleCombine(long currentSeed, long sourceSeed) {
			return currentSeed + sourceSeed;
		}

		public static long simpleXOR(long currentSeed, long sourceSeed) {
			return currentSeed ^ sourceSeed;
		}

		public static long rotateAndXOR(long currentSeed, long sourceSeed) {
			long result = currentSeed;
			// Linear Congruential Generator
			result = result * 6364136223846793005L + 1442695040888963407L;
			result ^= sourceSeed;
			result = Long.rotateLeft(result, 17);  // Rotate
			result ^= result >>> 32;  // Mix upper and lower bits
			return result;
		}
	}
}
