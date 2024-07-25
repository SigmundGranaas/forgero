package com.sigmundgranaas.forgero.minecraft.common.match.predicate;

import static com.sigmundgranaas.forgero.minecraft.common.match.MinecraftContextKeys.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.sigmundgranaas.forgero.core.model.match.builders.ElementParser;
import com.sigmundgranaas.forgero.core.model.match.builders.PredicateBuilder;
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
 *   "worldTimeQuantization": 1200
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
		return seedSources.stream()
				.mapToLong(source -> switch (source) {
					case BLOCK_POS -> context.get(BLOCK_TARGET).map(BlockPos::asLong).orElse(0L);
					case TARGET_ENTITY -> context.get(ENTITY_TARGET).map(Entity::getId).orElse(0);
					case SOURCE_ENTITY -> context.get(ENTITY).map(Entity::getId).orElse(0);
					case WORLD_TIME ->
							quantizeWorldTime(context.get(WORLD).map(World::getTime).orElse(0L), worldTimeQuantization);
					case NONE -> System.currentTimeMillis();
				})
				.reduce(System.currentTimeMillis(), (a, b) -> a ^ b); // Combine seeds using XOR
	}

	private long quantizeWorldTime(long worldTime, long quantizationInterval) {
		return quantizationInterval > 0 ? worldTime / quantizationInterval : worldTime;
	}

	@Override
	public boolean isDynamic() {
		return true;
	}

	public enum SeedSource {
		BLOCK_POS, TARGET_ENTITY, SOURCE_ENTITY, WORLD_TIME, NONE
	}

	/**
	 * Attempts to create a Matchable of type RandomPredicate from a JsonElement.
	 */
	public static class RandomPredicatePredicateBuilder implements PredicateBuilder {
		@Override
		public Optional<Matchable> create(JsonElement element) {
			return ElementParser.fromIdentifiedElement(element, ID)
					.map(jsonObject -> {
						float value = jsonObject.get("value").getAsFloat();
						int worldTimeQuantization = jsonObject.has("worldTimeQuantization")
								? jsonObject.get("worldTimeQuantization").getAsInt()
								: 0;
						List<SeedSource> seedSources = jsonObject.has("seed") ? parseSeedSources(jsonObject.getAsJsonArray("seed")) : Collections.emptyList();
						return new RandomPredicate(value, worldTimeQuantization, seedSources);
					});
		}

		private List<SeedSource> parseSeedSources(JsonArray jsonArray) {
			return StreamSupport.stream(jsonArray.spliterator(), false)
					.map(JsonElement::getAsString)
					.map(String::toUpperCase)
					.map(SeedSource::valueOf)
					.collect(Collectors.toList());
		}
	}
}
