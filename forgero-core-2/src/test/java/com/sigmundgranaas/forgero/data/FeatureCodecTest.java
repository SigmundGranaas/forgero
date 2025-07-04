package com.sigmundgranaas.forgero.data;

import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.sigmundgranaas.forgero.data.v3.codec.FeatureCodecs;
import com.sigmundgranaas.forgero.data.v3.codec.CodecConstants; // Added import
import com.sigmundgranaas.forgero.data.v3.dto.condition.TagMatchPredicateData;
import com.sigmundgranaas.forgero.data.v3.dto.feature.FeatureData;
import com.sigmundgranaas.forgero.data.v3.dto.feature.VeinMiningFeatureData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FeatureCodecTest {

	@Test
	void testParseVeinMiningFeatureMinimal() {
		String json = """
				{
				  "type": "forgero:vein_mining",
				  "selector": {
				    "type": "forgero:radius",
				    "radius": 1,
				    "tag": "forgero:vein_mining_ores"
				  },
				  "title": "feature.forgero.vein_mining.title",
				  "description": "feature.forgero.ore_vein_mining.description"
				}
				""";

		var result = FeatureCodecs.FEATURE_DATA_CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json));
		assertTrue(result.result().isPresent(), "Parsing minimal vein mining feature should succeed. " + result.error().map(DataResult.PartialResult::message).orElse(""));

		FeatureData data = result.result().get();
		assertTrue(data instanceof VeinMiningFeatureData);
		VeinMiningFeatureData feature = (VeinMiningFeatureData) data;

		assertEquals(CodecConstants.IDENTIFIER_FACTORY.of("forgero:vein_mining"), feature.type()); // Changed to OpenIdentifier
		assertEquals("feature.forgero.vein_mining.title", feature.title());
		assertEquals("feature.forgero.ore_vein_mining.description", feature.description());
		assertNull(feature.condition());

		assertNotNull(feature.selector());
		assertEquals(CodecConstants.IDENTIFIER_FACTORY.of("forgero:radius"), feature.selector().type()); // Changed to OpenIdentifier
		assertEquals(1, feature.selector().radius());
		assertEquals(CodecConstants.IDENTIFIER_FACTORY.of("forgero:vein_mining_ores"), feature.selector().tag()); // Changed to OpenIdentifier
	}

	@Test
	void testParseVeinMiningFeatureWithCondition() {
		String json = """
				{
				  "type": "forgero:vein_mining",
				  "selector": {
				    "type": "forgero:radius",
				    "radius": 2,
				    "tag": "forgero:another_tag"
				  },
				  "title": "Another title",
				  "description": "Another description",
				  "condition": {
				    "type": "forgero:root_has_tag",
				    "tag": "forgero:pickaxe"
				  }
				}
				""";

		var result = FeatureCodecs.FEATURE_DATA_CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json));
		assertTrue(result.result().isPresent(), "Parsing vein mining feature with condition should succeed. " + result.error().map(DataResult.PartialResult::message).orElse(""));

		FeatureData data = result.result().get();
		assertTrue(data instanceof VeinMiningFeatureData);
		VeinMiningFeatureData feature = (VeinMiningFeatureData) data;

		assertEquals(CodecConstants.IDENTIFIER_FACTORY.of("forgero:vein_mining"), feature.type()); // Changed to OpenIdentifier
		assertEquals("Another title", feature.title());
		assertEquals("Another description", feature.description());

		assertNotNull(feature.condition());
		assertEquals(1, feature.condition().predicates().size());
		assertTrue(feature.condition().predicates().get(0) instanceof TagMatchPredicateData);
		TagMatchPredicateData condition = (TagMatchPredicateData) feature.condition().predicates().get(0);
		assertEquals(CodecConstants.IDENTIFIER_FACTORY.of("forgero:root_has_tag"), condition.type()); // Changed to OpenIdentifier
		assertEquals(CodecConstants.IDENTIFIER_FACTORY.of("forgero:pickaxe"), condition.tag()); // Changed to OpenIdentifier

		assertNotNull(feature.selector());
		assertEquals(2, feature.selector().radius());
		assertEquals(CodecConstants.IDENTIFIER_FACTORY.of("forgero:another_tag"), feature.selector().tag()); // Changed to OpenIdentifier
	}

	@Test
	void testParseUnknownFeatureType() {
		String json = """
				{
				  "type": "forgero:non_existent_feature",
				  "title": "Bad feature",
				  "description": "This should fail"
				}
				""";
		var result = FeatureCodecs.FEATURE_DATA_CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json));
		assertTrue(result.result().isEmpty(), "Parsing an unknown feature type should fail.");
		assertTrue(result.error().isPresent(), "An error message should be present.");
		assertTrue(result.error().get().message().contains("Unknown feature type: forgero:non_existent_feature"));
	}
}
