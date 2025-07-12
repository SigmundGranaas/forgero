package com.sigmundgranaas.forgero.data.loading.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.data.loading.impl.codec.MaterialCodecs;
import com.sigmundgranaas.forgero.data.loading.impl.codec.CodecConstants;
import com.sigmundgranaas.forgero.data.loading.api.data.MaterialData;
import com.sigmundgranaas.forgero.data.loading.api.data.attribute.AttributeData;
import com.sigmundgranaas.forgero.data.loading.api.data.feature.VeinMiningFeatureData;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class MaterialDataCodecTest {

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

	private static final String FULL_MATERIAL_JSON = """
			{
			  "type": "forgero:material",
			  "name": "diamond",
			  "include": ["forgero:materials/mineral_base", "forgero:materials/smeltable_base"],
			  "tags": ["forgero:gem", "forgero:mineral", "forgero:tool_material"],
			  "attributes": [
			    { "id": "forgero:diamond-durability", "type": "forgero:durability", "computation": 1561 }
			  ],
			  "features": [
			    {
			      "type": "forgero:vein_mining",
			      "selector": { "type": "forgero:radius", "radius": 1, "tag": "forgero:vein_mining_ores" },
			      "title": "feature.forgero.vein_mining.title",
			      "description": "feature.forgero.ore_vein_mining.description"
			    }
			  ],
			  "properties": {
			    "forgero:tooltip": [
			      { "text": "A custom tooltip line" }
			    ],
			    "better_combat:attribute_container": {
			        "id": "better_combat:two_handed_spear"
			    }
			  }
			}
			""";

	@Test
	void testParseFullMaterial() {
		MaterialData data = parseSuccess(MaterialCodecs.MATERIAL_DATA_CODEC, FULL_MATERIAL_JSON);

		assertEquals(id("forgero:material"), data.type());
		assertEquals("diamond", data.name());

		assertNotNull(data.include());
		assertTrue(data.include().containsAll(List.of(id("forgero:materials/mineral_base"), id("forgero:materials/smeltable_base"))));

		assertNotNull(data.tags());
		assertTrue(data.tags().containsAll(List.of(id("forgero:gem"), id("forgero:mineral"), id("forgero:tool_material"))));

		assertNotNull(data.attributes());
		assertEquals(1, data.attributes().size());
		Optional<AttributeData> durability = data.attributes().stream().filter(a -> a.id().equals(id("forgero:diamond-durability"))).findFirst();
		assertTrue(durability.isPresent());
		assertEquals(1561f, durability.get().computation().value());

		assertNotNull(data.features());
		assertEquals(1, data.features().size());
		assertInstanceOf(VeinMiningFeatureData.class, data.features().get(0));

		assertNotNull(data.properties());
		assertEquals(2, data.properties().size());
		assertTrue(data.properties().containsKey("forgero:tooltip"));
		JsonElement tooltip = data.properties().get("forgero:tooltip");
		assertTrue(tooltip.isJsonArray());
		assertEquals(1, tooltip.getAsJsonArray().size());
		assertTrue(data.properties().containsKey("better_combat:attribute_container"));
	}

	@Test
	void testParseMinimalMaterial() {
		String json = """
				{
				  "type": "forgero:material",
				  "name": "minimal_stone"
				}
				""";

		MaterialData data = parseSuccess(MaterialCodecs.MATERIAL_DATA_CODEC, json);
		assertEquals(id("forgero:material"), data.type());
		assertEquals("minimal_stone", data.name());
		assertNull(data.include());
		assertNull(data.tags());
		assertNull(data.attributes());
		assertNull(data.features());
		assertNull(data.properties());
	}

	@Test
	void testParseMaterialMissingRequiredFields() {
		String json = "{ \"type\": \"forgero:material\" }"; // Missing 'name'
		parseFailure(MaterialCodecs.MATERIAL_DATA_CODEC, json, "No key name");
	}
}
