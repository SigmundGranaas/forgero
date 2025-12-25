package com.sigmundgranaas.forgero.predicate.minecraft.standalone;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.condition.api.DynamicCondition;
import com.sigmundgranaas.forgero.core.property.context.DynamicContext;
import com.sigmundgranaas.forgero.predicate.minecraft.MinecraftContextKeys;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * A dynamic condition that evaluates to true based on a random chance.
 * The randomness can be seeded using various context sources for deterministic behavior.
 *
 * <h3>Example (simple random):</h3>
 * <pre>
 * {
 *   "type": "forgero:random",
 *   "chance": 0.5
 * }
 * </pre>
 *
 * <h3>Example (seeded by world time and block position):</h3>
 * <pre>
 * {
 *   "type": "forgero:random",
 *   "chance": 0.5,
 *   "seed": ["world_time", "block_pos"],
 *   "world_time_quantization": 20
 * }
 * </pre>
 *
 * <p>Available seed sources: BLOCK_POS, TARGET_ENTITY, SOURCE_ENTITY, WORLD_TIME, NONE</p>
 */
public record RandomPredicate(
		float chance,
		int worldTimeQuantization,
		List<SeedSource> seedSources
) implements DynamicCondition {

	public static final OpenIdentifier TYPE = new OpenIdentifier("forgero", "random");

	private static final long FIXED_SEED = 0x12345678ABCDL;

	public static final Codec<SeedSource> SEED_SOURCE_CODEC = Codec.STRING.xmap(
			s -> SeedSource.valueOf(s.toUpperCase()),
			SeedSource::toString
	);

	public static final Codec<RandomPredicate> CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					Codec.FLOAT.fieldOf("chance").forGetter(RandomPredicate::chance),
					Codec.INT.optionalFieldOf("world_time_quantization", 0).forGetter(RandomPredicate::worldTimeQuantization),
					Codec.list(SEED_SOURCE_CODEC).optionalFieldOf("seed", Collections.emptyList()).forGetter(RandomPredicate::seedSources)
			).apply(instance, RandomPredicate::new));

	@Override
	public boolean test(DynamicContext context) {
		Random random;
		if (seedSources.isEmpty()) {
			random = new Random();
		} else {
			long seed = generateSeed(context);
			random = new Random(seed);
		}
		return random.nextFloat() < chance;
	}

	private long generateSeed(DynamicContext context) {
		Long[] seeds = seedSources.stream()
				.map(source -> getSeedValue(source, context))
				.toArray(Long[]::new);
		return combineSeedSources(seeds);
	}

	private long getSeedValue(SeedSource source, DynamicContext context) {
		return switch (source) {
			case BLOCK_POS -> context.get(MinecraftContextKeys.BLOCK_POS)
					.map(BlockPos::asLong)
					.orElse(0L);
			case TARGET_ENTITY -> context.get(MinecraftContextKeys.TARGET_ENTITY)
					.map(Entity::getId)
					.map(Integer::longValue)
					.orElse(0L);
			case SOURCE_ENTITY -> context.get(MinecraftContextKeys.SOURCE_ENTITY)
					.map(Entity::getId)
					.map(Integer::longValue)
					.orElse(0L);
			case WORLD_TIME -> context.get(MinecraftContextKeys.WORLD)
					.map(World::getTime)
					.map(time -> computeWorldTimeSeed(time, worldTimeQuantization))
					.orElse(0L);
			case NONE -> 1L;
		};
	}

	private static long combineSeedSources(Long... seedSources) {
		long combinedSeed = FIXED_SEED;
		for (long sourceSeed : seedSources) {
			combinedSeed = combineSeed(combinedSeed, sourceSeed);
		}
		return combinedSeed;
	}

	private static long combineSeed(long currentSeed, long sourceSeed) {
		long result = currentSeed;
		// Linear Congruential Generator
		result = result * 6364136223846793005L + 1442695040888963407L;
		result ^= sourceSeed;
		result = Long.rotateLeft(result, 17);
		result ^= result >>> 32;
		return result;
	}

	private static long computeWorldTimeSeed(long worldTime, int quantization) {
		if (quantization > 0) {
			worldTime = worldTime / quantization;
		}
		double angle = (worldTime % 360) * (Math.PI / 180);
		return (long) (Math.sin(angle) * Long.MAX_VALUE);
	}

	@Override
	public OpenIdentifier type() {
		return TYPE;
	}

	public enum SeedSource {
		BLOCK_POS, TARGET_ENTITY, SOURCE_ENTITY, WORLD_TIME, NONE
	}
}
