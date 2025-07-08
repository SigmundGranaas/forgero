package com.sigmundgranaas.forgero.data;

import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.sigmundgranaas.forgero.core.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.data.v3.codec.FeatureCodecs;
import com.sigmundgranaas.forgero.data.v3.codec.CodecConstants;
import com.sigmundgranaas.forgero.data.v3.dto.condition.TagMatchPredicateData;
import com.sigmundgranaas.forgero.data.v3.dto.feature.FeatureData;
import com.sigmundgranaas.forgero.data.v3.dto.feature.VeinMiningFeatureData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FeatureCodecTest {

	// region Helpers
	private <T> T parseSuccess(Codec<T> codec, String json) {
		DataResult<T> result = codec.parse(JsonOps.INSTANCE, JsonParser.parseString(json));
		assertTrue(result.result().isPresent(), "Parsing should succeed. Error: " + result.error().map(DataResult.PartialResult::message).orElse("No error message"));
		return result.result().get();
	}

	private <T> void parseFailure(Codec<T> codec, String json, String expectedError) {
		DataResult<T> result = codec.parse(JsonOps.INSTANCE, JsonParser.parseString(json));
		assertTrue(result.result().isEmpty(), "Parsing should fail for: " + json);
		assertTrue(result.error().isPresent(), "An error message should be present.");
		assertTrue(result.error().get().message().contains(expectedError), "Error message mismatch. Expected to contain '" + expectedError + "', but was: " + result.error().get().message());
	}

	private OpenIdentifier id(String id) {
		return CodecConstants.IDENTIFIER_FACTORY.of(id);
	}
	// endregion

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

		FeatureData data = parseSuccess(FeatureCodecs.FEATURE_DATA_CODEC, json);
		assertInstanceOf(VeinMiningFeatureData.class, data);
		VeinMiningFeatureData feature = (VeinMiningFeatureData) data;

		assertEquals(id("forgero:vein_mining"), feature.type());
		assertEquals("feature.forgero.vein_mining.title", feature.title());
		assertEquals("feature.forgero.ore_vein_mining.description", feature.description());
		assertNull(feature.condition());

		assertNotNull(feature.selector());
		assertEquals(id("forgero:radius"), feature.selector().type());
		assertEquals(1, feature.selector().radius());
		assertEquals(id("forgero:vein_mining_ores"), feature.selector().tag());
	}

	@Test
	void testParseVeinMiningFeatureWithCondition() {
		String json = """
				{
				  "type": "forgero:vein_mining",
				  "selector": { "type": "forgero:radius", "radius": 2, "tag": "forgero:another_tag" },
				  "title": "Another title",
				  "description": "Another description",
				  "condition": { "type": "forgero:root_has_tag", "tag": "forgero:pickaxe" }
				}
				""";

		FeatureData data = parseSuccess(FeatureCodecs.FEATURE_DATA_CODEC, json);
		assertInstanceOf(VeinMiningFeatureData.class, data);
		VeinMiningFeatureData feature = (VeinMiningFeatureData) data;

		assertEquals(id("forgero:vein_mining"), feature.type());
		assertEquals("Another title", feature.title());
		assertEquals("Another description", feature.description());

		assertNotNull(feature.condition());
		assertEquals(1, feature.condition().predicates().size());
		assertInstanceOf(TagMatchPredicateData.class, feature.condition().predicates().get(0));
		TagMatchPredicateData condition = (TagMatchPredicateData) feature.condition().predicates().get(0);
		assertEquals(id("forgero:root_has_tag"), condition.type());
		assertEquals(id("forgero:pickaxe"), condition.tag());

		assertNotNull(feature.selector());
		assertEquals(2, feature.selector().radius());
		assertEquals(id("forgero:another_tag"), feature.selector().tag());
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
		parseFailure(FeatureCodecs.FEATURE_DATA_CODEC, json, "Unknown feature type: forgero:non_existent_feature");
	}
}
