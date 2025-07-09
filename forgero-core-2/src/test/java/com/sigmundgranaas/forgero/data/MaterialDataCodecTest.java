package com.sigmundgranaas.forgero.data;

import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.sigmundgranaas.forgero.core.identifier.api.OpenIdentifier;
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
			    { "id": "forgero:diamond-durability", "type": "forgero:durability", "computation": 1561 },
			    { "id": "forgero:diamond-mining_level", "type": "forgero:mining_level", "computation": 3 },
			    { "id": "forgero:diamond-composite-mining-speed", "type": "forgero:mining_speed", "composite": "forgero:material-mining-speed", "computation": { "add": 8 } }
			  ],
			  "features": [
			    {
			      "type": "forgero:vein_mining",
			      "selector": { "type": "forgero:radius", "radius": 1, "tag": "forgero:vein_mining_ores" },
			      "title": "feature.forgero.vein_mining.title",
			      "description": "feature.forgero.ore_vein_mining.description"
			    }
			  ]
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
		assertEquals(3, data.attributes().size());
		Optional<AttributeData> durability = data.attributes().stream().filter(a -> a.id().equals(id("forgero:diamond-durability"))).findFirst();
		assertTrue(durability.isPresent());
		assertEquals(1561f, durability.get().computation().value());

		assertNotNull(data.features());
		assertEquals(1, data.features().size());
		assertInstanceOf(VeinMiningFeatureData.class, data.features().get(0));
		VeinMiningFeatureData veinMining = (VeinMiningFeatureData) data.features().get(0);
		assertEquals(id("forgero:vein_mining"), veinMining.type());
		assertEquals(id("forgero:radius"), veinMining.selector().type());
		assertEquals(1, veinMining.selector().radius());
		assertEquals(id("forgero:vein_mining_ores"), veinMining.selector().tag());
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
	}

	@Test
	void testParseMaterialWithOnlyTagsAndAttributes() {
		String json = """
				{
				  "type": "forgero:material",
				  "name": "basic_wood",
				  "tags": ["forgero:wood", "forgero:common"],
				  "attributes": [
				    { "id": "forgero:wood-hardness", "type": "forgero:hardness", "computation": 2.0 }
				  ]
				}
				""";

		MaterialData data = parseSuccess(MaterialCodecs.MATERIAL_DATA_CODEC, json);
		assertEquals(id("forgero:material"), data.type());
		assertEquals("basic_wood", data.name());

		assertNotNull(data.tags());
		assertTrue(data.tags().containsAll(List.of(id("forgero:wood"), id("forgero:common"))));

		assertNotNull(data.attributes());
		assertEquals(1, data.attributes().size());
		AttributeData hardness = data.attributes().get(0);
		assertEquals(id("forgero:wood-hardness"), hardness.id());
		assertEquals(id("forgero:hardness"), hardness.type());
		assertEquals(2.0f, hardness.computation().value());

		assertNull(data.include());
		assertNull(data.features());
	}

	@Test
	void testParseMaterialMissingRequiredFields() {
		String json = "{ \"type\": \"forgero:material\" }"; // Missing 'name'
		parseFailure(MaterialCodecs.MATERIAL_DATA_CODEC, json, "No key name");
	}
}
