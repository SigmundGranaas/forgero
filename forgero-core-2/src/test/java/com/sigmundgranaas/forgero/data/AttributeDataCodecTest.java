package com.sigmundgranaas.forgero.data;

import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.sigmundgranaas.forgero.core.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.data.loading.impl.codec.AttributeCodecs;
import com.sigmundgranaas.forgero.data.loading.impl.codec.CodecConstants;
import com.sigmundgranaas.forgero.data.loading.api.data.attribute.AttributeData;
import com.sigmundgranaas.forgero.data.loading.api.data.condition.TagMatchPredicateData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AttributeDataCodecTest {

	// region Helpers
	private <T> T parseSuccess(Codec<T> codec, String json) {
		DataResult<T> result = codec.parse(JsonOps.INSTANCE, JsonParser.parseString(json));
		assertTrue(result.result().isPresent(), "Parsing should succeed. Error: " + result.error().map(DataResult.PartialResult::message).orElse("No error message"));
		return result.result().get();
	}

	private OpenIdentifier id(String id) {
		return CodecConstants.IDENTIFIER_FACTORY.of(id);
	}
	// endregion

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

		AttributeData data = parseSuccess(AttributeCodecs.ATTRIBUTE_DATA_CODEC, json);

		assertEquals(id("forgero:diamond-composite-mining-speed"), data.id());
		assertEquals(id("forgero:mining_speed"), data.type());
		assertEquals(id("forgero:material-mining-speed"), data.composite());
		assertEquals(8f, data.computation().value());
		assertEquals(AttributeCodecs.ADDITION_OPERATOR, data.computation().operator());
		assertEquals(AttributeCodecs.BASE_ORDER, data.computation().order());

		assertNotNull(data.condition());
		assertEquals(1, data.condition().predicates().size());
		var predicate = data.condition().predicates().get(0);
		assertInstanceOf(TagMatchPredicateData.class, predicate);
		assertEquals(id("forgero:self_has_tag"), predicate.type());
		assertEquals(id("forgero:gem"), ((TagMatchPredicateData) predicate).tag());
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

		AttributeData data = parseSuccess(AttributeCodecs.ATTRIBUTE_DATA_CODEC, json);

		assertEquals(id("forgero:diamond-durability"), data.id());
		assertEquals(id("forgero:durability"), data.type());
		assertNull(data.composite(), "Composite should be null when not present");
		assertNull(data.condition(), "Condition should be null when not present");

		assertEquals(1561f, data.computation().value());
		assertEquals(AttributeCodecs.ADDITION_OPERATOR, data.computation().operator());
		assertEquals(AttributeCodecs.BASE_ORDER, data.computation().order());
	}
}
