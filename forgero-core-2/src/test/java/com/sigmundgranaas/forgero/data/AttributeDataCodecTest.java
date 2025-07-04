package com.sigmundgranaas.forgero.data;

import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.sigmundgranaas.forgero.data.v3.codec.AttributeCodecs;
import com.sigmundgranaas.forgero.data.v3.codec.CodecConstants; // Added import
import com.sigmundgranaas.forgero.data.v3.dto.attribute.AttributeData;
import com.sigmundgranaas.forgero.data.v3.dto.condition.TagMatchPredicateData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AttributeDataCodecTest {

	@Test
	void testParseFullAttribute() {
		String json = """
				{
				  "id": "forgero:diamond-composite-mining-speed",
				  "type": "forgero:mining_speed",
				  "composite": "forgero:material-mining-speed",
				  "condition": { "type": "forgero:self_has_tag", "tag": "forgero:gem" },
				  "computation": { "add": 8, "order": "forgero:base" }
				}
				""";

		var result = AttributeCodecs.ATTRIBUTE_DATA_CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json));
		assertTrue(result.result().isPresent(), "Parsing attribute data should succeed. " + result.error().map(DataResult.PartialResult::message).orElse(""));

		AttributeData data = result.result().get();
		// Assertions changed to use OpenIdentifier
		assertEquals(CodecConstants.IDENTIFIER_FACTORY.of("forgero:diamond-composite-mining-speed"), data.id());
		assertEquals(CodecConstants.IDENTIFIER_FACTORY.of("forgero:mining_speed"), data.type());
		assertEquals(CodecConstants.IDENTIFIER_FACTORY.of("forgero:material-mining-speed"), data.composite());
		assertEquals(8f, data.computation().value());
		assertEquals(AttributeCodecs.ADDITION_OPERATOR, data.computation().operator());
		assertEquals(AttributeCodecs.BASE_ORDER, data.computation().order());


		// Assertions for the ConditionData structure
		assertNotNull(data.condition());
		assertEquals(1, data.condition().predicates().size());
		var predicate = data.condition().predicates().get(0);
		assertTrue(predicate instanceof TagMatchPredicateData);
		assertEquals(CodecConstants.IDENTIFIER_FACTORY.of("forgero:self_has_tag"), predicate.type());
		assertEquals(CodecConstants.IDENTIFIER_FACTORY.of("forgero:gem"), ((TagMatchPredicateData) predicate).tag());
	}

	@Test
	void testParseMinimalAttribute() {
		String json = """
				{
				  "id": "forgero:diamond-durability",
				  "type": "forgero:durability",
				  "computation": 1561
				}
				""";

		var result = AttributeCodecs.ATTRIBUTE_DATA_CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json));
		assertTrue(result.result().isPresent(), "Parsing minimal attribute data should succeed");

		AttributeData data = result.result().get();
		// Assertions changed to use OpenIdentifier
		assertEquals(CodecConstants.IDENTIFIER_FACTORY.of("forgero:diamond-durability"), data.id());
		assertEquals(CodecConstants.IDENTIFIER_FACTORY.of("forgero:durability"), data.type());
		assertNull(data.composite(), "Composite should be null when not present");
		assertNull(data.condition(), "Condition should be null when not present");

		assertEquals(1561f, data.computation().value());
		assertEquals(AttributeCodecs.ADDITION_OPERATOR, data.computation().operator());
		assertEquals(AttributeCodecs.BASE_ORDER, data.computation().order());
	}
}
