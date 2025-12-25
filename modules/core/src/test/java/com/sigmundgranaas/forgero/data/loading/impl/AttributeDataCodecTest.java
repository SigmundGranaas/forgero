package com.sigmundgranaas.forgero.data.loading.impl;

import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.tags.engine.TagGraph;
import com.sigmundgranaas.forgero.core.condition.api.DynamicCondition;
import com.sigmundgranaas.forgero.core.condition.api.StaticCondition;
import com.sigmundgranaas.forgero.core.condition.predicate.TagMatchCondition;
import com.sigmundgranaas.forgero.data.loading.api.data.attribute.AttributeData;
import com.sigmundgranaas.forgero.data.loading.impl.codec.AttributeCodecs;
import com.sigmundgranaas.forgero.data.loading.impl.codec.CodecConstants;
import com.sigmundgranaas.forgero.core.condition.api.ConditionCodec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.sigmundgranaas.forgero.testutils.TestIdentifiers.id;
import static org.junit.jupiter.api.Assertions.*;

class AttributeDataCodecTest {

	private Codec<AttributeData> attributeDataCodec;

	@BeforeEach
	void setUp() {
		Map<String, Codec<? extends StaticCondition>> staticCodecs = new HashMap<>();
		Map<OpenIdentifier, Set<OpenIdentifier>> tagMap = new HashMap<>();
		tagMap.put(id("pickaxe"), new HashSet<>());
		staticCodecs.put("forgero:self_has_tag", TagMatchCondition.codec(() -> new TagGraph(tagMap)));
		Map<String, Codec<? extends DynamicCondition>> dynamicCodecs = new HashMap<>();
		ConditionCodec conditionCodec = new ConditionCodec(staticCodecs, dynamicCodecs);
		this.attributeDataCodec = AttributeCodecs.create(conditionCodec);
	}

	private <T> T parseSuccess(Codec<T> codec, String json) {
		DataResult<T> result = codec.parse(JsonOps.INSTANCE, JsonParser.parseString(json));
		assertTrue(result.result().isPresent(), "Parsing should succeed. Error: " + result.error().map(DataResult.PartialResult::message).orElse("No error message"));
		return result.result().get();
	}

	private OpenIdentifier id(String id) {
		return CodecConstants.IDENTIFIER_FACTORY.of(id);
	}

	@Test
	void testParseFullAttribute() {
		String json = """
				{
				  "id": "forgero:diamond-scoped-mining-speed",
				  "type": "forgero:mining_speed",
				  "condition": { "type": "forgero:self_has_tag", "tag": "forgero:gem" },
				  "computation": { "add": 8 }
				}
				""";

		AttributeData data = parseSuccess(attributeDataCodec, json);

		assertEquals(id("forgero:diamond-scoped-mining-speed"), data.id());
		assertEquals(id("forgero:mining_speed"), data.type());
		assertEquals(8f, data.computation().value());
		assertEquals(AttributeCodecs.ADDITION_OPERATOR, data.computation().operator());
		assertEquals(AttributeCodecs.BASE_ORDER, data.computation().order());

		assertNotNull(data.condition());
		assertEquals(1, data.condition().staticConditions().size());
		assertTrue(data.condition().dynamicConditions().isEmpty());
		var predicate = data.condition().staticConditions().get(0);
		assertInstanceOf(TagMatchCondition.class, predicate);
		assertEquals(id("forgero:self_has_tag"), predicate.type());
		assertEquals(id("forgero:gem"), ((TagMatchCondition) predicate).tag());
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

		AttributeData data = parseSuccess(attributeDataCodec, json);

		assertEquals(id("forgero:diamond-durability"), data.id());
		assertEquals(id("forgero:durability"), data.type());

		assertNull(data.condition());
		assertEquals(1561f, data.computation().value());
		assertEquals(AttributeCodecs.ADDITION_OPERATOR, data.computation().operator());
		assertEquals(AttributeCodecs.BASE_ORDER, data.computation().order());
	}
}
