package com.sigmundgranaas.forgero.minecraft.common.match.predicate;

import static com.sigmundgranaas.forgero.minecraft.common.match.MinecraftContextKeys.*;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
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

	private static final long FIXED_SEED = 0x12345678ABCDL; // A fixed seed to mix with context-based seeds


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
		long seed = FIXED_SEED;
		for (SeedSource source : seedSources) {
			long sourceSeed = switch (source) {
				case BLOCK_POS -> context.get(BLOCK_TARGET).map(BlockPos::asLong).orElse(0L);
				case TARGET_ENTITY -> context.get(ENTITY_TARGET).map(Entity::getId).orElse(0);
				case SOURCE_ENTITY -> context.get(ENTITY).map(Entity::getId).orElse(0);
				case WORLD_TIME -> {
					long worldTime = context.get(WORLD).map(World::getTime).orElse(0L);
					if (worldTimeQuantization > 0) {
						worldTime = worldTime / worldTimeQuantization;
					}
					// Sine function to create more variation even with very small time changes.
					// Example: world time goes from 1 to 2
					double angle = (worldTime % 360) * (Math.PI / 180);
					yield (long) (MathHelper.sin((float) angle) * Long.MAX_VALUE);
				}
				case NONE -> 1L;
			};
			seed = Long.rotateLeft(seed, 5) ^ sourceSeed;
		}
		return seed;
	}

	@Override
	public boolean isDynamic() {
		return true;
	}

	public enum SeedSource {
		BLOCK_POS, TARGET_ENTITY, SOURCE_ENTITY, WORLD_TIME, NONE
	}
}
