package com.sigmundgranaas.forgero.data;

import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.sigmundgranaas.forgero.data.v3.codec.StaticPartCodecs;
import com.sigmundgranaas.forgero.data.v3.codec.CodecConstants; // Added import
import com.sigmundgranaas.forgero.data.v3.dto.StaticPartData;
import com.sigmundgranaas.forgero.data.v3.dto.attribute.AttributeData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StaticPartDataCodecTest {

	@Test
	void testParseFullStaticPart() {
		String json = """
				{
				  "type": "forgero:static_part",
				  "name": "custom_stone",
				  "include": ["forgero:materials/stone_base"],
				  "tags": ["forgero:stone", "forgero:special"],
				  "attributes": [
				    {
				      "id": "forgero:custom-hardness",
				      "type": "forgero:hardness",
				      "computation": 5.0
				    }
				  ],
				  "features": []
				}
				""";

		DataResult<StaticPartData> result = StaticPartCodecs.STATIC_PART_DATA_CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json));
		assertTrue(result.result().isPresent(), "Parsing full static part data should succeed. " + result.error().map(DataResult.PartialResult::message).orElse(""));

		StaticPartData data = result.result().get();
		assertEquals(CodecConstants.IDENTIFIER_FACTORY.of("forgero:static_part"), data.type()); // Changed to OpenIdentifier
		assertEquals("custom_stone", data.name());

		assertNotNull(data.include());
		assertEquals(1, data.include().size());
		assertTrue(data.include().contains(CodecConstants.IDENTIFIER_FACTORY.of("forgero:materials/stone_base"))); // Changed to OpenIdentifier

		assertNotNull(data.tags());
		assertEquals(2, data.tags().size());
		assertTrue(data.tags().contains(CodecConstants.IDENTIFIER_FACTORY.of("forgero:stone"))); // Changed to OpenIdentifier
		assertTrue(data.tags().contains(CodecConstants.IDENTIFIER_FACTORY.of("forgero:special"))); // Changed to OpenIdentifier

		assertNotNull(data.attributes());
		assertEquals(1, data.attributes().size());
		AttributeData hardness = data.attributes().get(0);
		assertEquals(CodecConstants.IDENTIFIER_FACTORY.of("forgero:custom-hardness"), hardness.id()); // Changed to OpenIdentifier
		assertEquals(CodecConstants.IDENTIFIER_FACTORY.of("forgero:hardness"), hardness.type()); // Changed to OpenIdentifier
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

		DataResult<StaticPartData> result = StaticPartCodecs.STATIC_PART_DATA_CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json));
		assertTrue(result.result().isPresent(), "Parsing minimal static part data should succeed. " + result.error().map(DataResult.PartialResult::message).orElse(""));

		StaticPartData data = result.result().get();
		assertEquals(CodecConstants.IDENTIFIER_FACTORY.of("forgero:static_part"), data.type()); // Changed to OpenIdentifier
		assertEquals("simple_stick", data.name());
		assertNull(data.include());
		assertNull(data.tags());
		assertNull(data.attributes());
		assertNull(data.features());
	}

	@Test
	void testParseStaticPartMissingRequiredFields() {
		String json = """
				{
				  "type": "forgero:static_part"
				}
				"""; // Missing 'name'

		DataResult<StaticPartData> result = StaticPartCodecs.STATIC_PART_DATA_CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json));
		assertTrue(result.error().isPresent(), "Parsing static part data missing 'name' should produce an error.");
		String errorMessage = result.error().get().message();
		assertTrue(errorMessage.contains("No key name"), "Error message should indicate missing 'name'. Actual: " + errorMessage);
	}
}
