package com.sigmundgranaas.forgero.property;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import com.sigmundgranaas.forgero.minecraft.common.match.predicate.RandomPredicate;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PropertyInitializerTest {

	@Test
	void testSerializationDeserialization() {
		RandomPredicate predicate = new RandomPredicate(0.5f, 1200, Arrays.asList(RandomPredicate.SeedSource.BLOCK_POS, RandomPredicate.SeedSource.WORLD_TIME));
		String json = RandomPredicate.CODEC.encodeStart(JsonOps.INSTANCE, predicate).result().orElseThrow().toString();
		RandomPredicate deserializedPredicate = RandomPredicate.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).result().orElseThrow();

		assertEquals(predicate, deserializedPredicate);
	}

}
