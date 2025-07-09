package com.sigmundgranaas.forgero.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.data.loading.api.data.ShapeData;
import com.sigmundgranaas.forgero.data.loading.api.data.attribute.AttributeData;
import com.sigmundgranaas.forgero.data.loading.api.data.attribute.AttributeDataImpl;
import com.sigmundgranaas.forgero.data.loading.api.data.attribute.ComputationData;
import com.sigmundgranaas.forgero.data.loading.api.data.feature.VeinMiningFeatureData;
import com.sigmundgranaas.forgero.data.loading.api.data.feature.VeinMiningSelectorData;
import com.sigmundgranaas.forgero.data.loading.impl.codec.CodecConstants;
import com.sigmundgranaas.forgero.data.loading.impl.codec.ShapeCodecs;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ShapeDataCodecTest {

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

	private static final String FULL_SHAPE_JSON = """
			{
			  "type": "forgero:shape",
			  "name": "pickaxe_head",
			  "include": ["forgero:shapes/tool_head_base"],
			  "tags": ["forgero:pickaxe_head_shape", "forgero:tool_part_shape"],
			  "attributes": [
			    { "id": "forgero:head-mining_speed-composite", "type": "forgero:mining_speed", "composite": "forgero:tool-mining-speed", "computation": { "multiply": 1.25 } }
			  ],
			  "features": [
			    { "type": "forgero:vein_mining", "selector": { "type": "forgero:radius", "radius": 1, "tag": "forgero:vein_mining_ores" }, "title": "feature.forgero.vein_mining.title", "description": "feature.forgero.ore_vein_mining.description" }
			  ],
			  "properties": {
			    "forgero:visual": { "model": "forgero:item/pickaxe_head_model" },
			    "forgero:hitbox_size": 1.5
			  }
			}
			""";

	@Test
	void testParseFullShape() {
		ShapeData data = parseSuccess(ShapeCodecs.SHAPE_DATA_CODEC, FULL_SHAPE_JSON);

		assertEquals(id("forgero:shape"), data.type());
		assertEquals("pickaxe_head", data.name());

		assertNotNull(data.include());
		assertTrue(data.include().contains(id("forgero:shapes/tool_head_base")));

		assertNotNull(data.tags());
		assertTrue(data.tags().containsAll(List.of(id("forgero:pickaxe_head_shape"), id("forgero:tool_part_shape"))));

		assertNotNull(data.attributes());
		assertEquals(1, data.attributes().size());
		Optional<AttributeData> miningSpeed = data.attributes().stream().filter(a -> a.id().equals(id("forgero:head-mining_speed-composite"))).findFirst();
		assertTrue(miningSpeed.isPresent());
		assertEquals(1.25f, miningSpeed.get().computation().value());

		assertNotNull(data.features());
		assertEquals(1, data.features().size());
		assertInstanceOf(VeinMiningFeatureData.class, data.features().get(0));

		assertNotNull(data.properties());
		assertEquals(2, data.properties().size());
		assertTrue(data.properties().containsKey("forgero:visual"));
		assertTrue(data.properties().get("forgero:visual").isJsonObject());
		assertTrue(data.properties().containsKey("forgero:hitbox_size"));
		assertTrue(data.properties().get("forgero:hitbox_size").isJsonPrimitive());
		assertEquals(1.5, data.properties().get("forgero:hitbox_size").getAsDouble(), 0.001);
	}

	@Test
	void testParseMinimalShape() {
		String json = """
				{
				  "type": "forgero:shape",
				  "name": "basic_handle"
				}
				""";

		ShapeData data = parseSuccess(ShapeCodecs.SHAPE_DATA_CODEC, json);
		assertEquals(id("forgero:shape"), data.type());
		assertEquals("basic_handle", data.name());
		assertNull(data.include());
		assertNull(data.tags());
		assertNull(data.attributes());
		assertNull(data.features());
		assertNull(data.properties());
	}

	@Test
	void testParseShapeMissingRequiredFields() {
		String json = "{ \"type\": \"forgero:shape\" }"; // Missing 'name'
		parseFailure(ShapeCodecs.SHAPE_DATA_CODEC, json, "No key name");
	}
}
