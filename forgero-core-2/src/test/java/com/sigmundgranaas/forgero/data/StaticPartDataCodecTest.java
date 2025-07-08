package com.sigmundgranaas.forgero.data;

import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.sigmundgranaas.forgero.core.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.data.v3.codec.StaticPartCodecs;
import com.sigmundgranaas.forgero.data.v3.codec.CodecConstants;
import com.sigmundgranaas.forgero.data.v3.dto.StaticPartData;
import com.sigmundgranaas.forgero.data.v3.dto.attribute.AttributeData;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StaticPartDataCodecTest {

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
	void testParseFullStaticPart() {
		String json = """
				{
				  "type": "forgero:static_part",
				  "name": "custom_stone",
				  "include": ["forgero:materials/stone_base"],
				  "tags": ["forgero:stone", "forgero:special"],
				  "attributes": [
				    { "id": "forgero:custom-hardness", "type": "forgero:hardness", "computation": 5.0 }
				  ],
				  "features": []
				}
				""";

		StaticPartData data = parseSuccess(StaticPartCodecs.STATIC_PART_DATA_CODEC, json);
		assertEquals(id("forgero:static_part"), data.type());
		assertEquals("custom_stone", data.name());

		assertNotNull(data.include());
		assertTrue(data.include().contains(id("forgero:materials/stone_base")));

		assertNotNull(data.tags());
		assertTrue(data.tags().containsAll(List.of(id("forgero:stone"), id("forgero:special"))));

		assertNotNull(data.attributes());
		assertEquals(1, data.attributes().size());
		AttributeData hardness = data.attributes().get(0);
		assertEquals(id("forgero:custom-hardness"), hardness.id());
		assertEquals(id("forgero:hardness"), hardness.type());
		assertEquals(5.0f, hardness.computation().value());

		assertNotNull(data.features());
		assertTrue(data.features().isEmpty());
	}

	@Test
	void testParseMinimalStaticPart() {
		String json = """
				{
				  "type": "forgero:static_part",
				  "name": "simple_stick"
				}
				""";

		StaticPartData data = parseSuccess(StaticPartCodecs.STATIC_PART_DATA_CODEC, json);
		assertEquals(id("forgero:static_part"), data.type());
		assertEquals("simple_stick", data.name());
		assertNull(data.include());
		assertNull(data.tags());
		assertNull(data.attributes());
		assertNull(data.features());
	}

	@Test
	void testParseStaticPartMissingRequiredFields() {
		String json = "{ \"type\": \"forgero:static_part\" }"; // Missing 'name'
		parseFailure(StaticPartCodecs.STATIC_PART_DATA_CODEC, json, "No key name");
	}
}
