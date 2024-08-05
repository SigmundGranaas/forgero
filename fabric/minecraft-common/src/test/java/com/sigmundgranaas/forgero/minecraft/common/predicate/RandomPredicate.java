package com.sigmundgranaas.forgero.minecraft.common.predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.minecraft.common.match.predicate.RandomPredicate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class RandomPredicateTest {

	@Test
	void testSerializationDeserialization() {
		RandomPredicate predicate = new RandomPredicate(0.5f, 1200, Arrays.asList(RandomPredicate.SeedSource.BLOCK_POS, RandomPredicate.SeedSource.WORLD_TIME));
		String json = RandomPredicate.CODEC.encodeStart(JsonOps.INSTANCE, predicate).result().orElseThrow().toString();
		RandomPredicate deserializedPredicate = RandomPredicate.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).result().orElseThrow();

		assertEquals(predicate, deserializedPredicate);
	}

	@ParameterizedTest
	@MethodSource("provideValidConfigurations")
	void testValidConfigurations(float value, int worldTimeQuantization, List<RandomPredicate.SeedSource> seedSources) {
		RandomPredicate predicate = new RandomPredicate(value, worldTimeQuantization, seedSources);
		String json = RandomPredicate.CODEC.encodeStart(JsonOps.INSTANCE, predicate).result().orElseThrow().toString();
		RandomPredicate deserializedPredicate = RandomPredicate.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).result().orElseThrow();

		assertEquals(predicate, deserializedPredicate);
		assertEquals(value, deserializedPredicate.value());
		assertEquals(worldTimeQuantization, deserializedPredicate.worldTimeQuantization());
		assertEquals(seedSources, deserializedPredicate.seedSources());
	}

	private static Stream<Arguments> provideValidConfigurations() {
		return Stream.of(
				Arguments.of(0.5f, 0, Collections.emptyList()),
				Arguments.of(0.3f, 1200, Collections.singletonList(RandomPredicate.SeedSource.WORLD_TIME)),
				Arguments.of(0.7f, 0, Arrays.asList(RandomPredicate.SeedSource.BLOCK_POS, RandomPredicate.SeedSource.TARGET_ENTITY)),
				Arguments.of(1.0f, 0, Collections.singletonList(RandomPredicate.SeedSource.NONE)),
				Arguments.of(0.0f, 600, Arrays.asList(RandomPredicate.SeedSource.SOURCE_ENTITY, RandomPredicate.SeedSource.WORLD_TIME))
		);
	}

	@Test
	void testRandomnessWithNoSeedSource() {
		RandomPredicate predicate = new RandomPredicate(0.5f, 0, Collections.emptyList());
		MatchContext context = new MatchContext();

		int trueCount = 0;
		int iterations = 1000;

		for (int i = 0; i < iterations; i++) {
			if (predicate.test(null, context)) {
				trueCount++;
			}
		}

		// Check if the randomness is roughly 50% (allowing for some variance)
		assertTrue(trueCount > 400 && trueCount < 600, "Random distribution should be roughly 50%");
	}
}
