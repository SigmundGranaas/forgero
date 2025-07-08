package com.sigmundgranaas.forgero.data;

import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.sigmundgranaas.forgero.core.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.data.codec.ConditionCodecs;
import com.sigmundgranaas.forgero.data.codec.CodecConstants;
import com.sigmundgranaas.forgero.data.dto.condition.AndPredicateData;
import com.sigmundgranaas.forgero.data.dto.condition.ConditionData;
import com.sigmundgranaas.forgero.data.dto.condition.InSlotTypePredicateData;
import com.sigmundgranaas.forgero.data.dto.condition.NotPredicateData;
import com.sigmundgranaas.forgero.data.dto.condition.OrPredicateData;
import com.sigmundgranaas.forgero.data.dto.condition.SlotContainsPredicateData;
import com.sigmundgranaas.forgero.data.dto.condition.TagMatchPredicateData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConditionCodecTest {

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
	void testParseSingleConditionObject() {
		String json = """
				{
				  "type": "forgero:self_has_tag",
				  "tag": "forgero:gem"
				}
				""";
		ConditionData data = parseSuccess(ConditionCodecs.CONDITION_DATA_CODEC, json);
		assertEquals(1, data.predicates().size());
		assertInstanceOf(TagMatchPredicateData.class, data.predicates().get(0));

		TagMatchPredicateData predicate = (TagMatchPredicateData) data.predicates().get(0);
		assertEquals(id("forgero:self_has_tag"), predicate.type());
		assertEquals(id("forgero:gem"), predicate.tag());
	}

	@Test
	void testParseConditionArray() {
		String json = """
				[
				  { "type": "forgero:root_has_tag", "tag": "forgero:pickaxe" },
				  { "type": "forgero:self_has_tag", "tag": "forgero:metal" }
				]
				""";
		ConditionData data = parseSuccess(ConditionCodecs.CONDITION_DATA_CODEC, json);
		assertEquals(2, data.predicates().size());

		assertInstanceOf(TagMatchPredicateData.class, data.predicates().get(0));
		TagMatchPredicateData first = (TagMatchPredicateData) data.predicates().get(0);
		assertEquals(id("forgero:root_has_tag"), first.type());
		assertEquals(id("forgero:pickaxe"), first.tag());

		assertInstanceOf(TagMatchPredicateData.class, data.predicates().get(1));
		TagMatchPredicateData second = (TagMatchPredicateData) data.predicates().get(1);
		assertEquals(id("forgero:self_has_tag"), second.type());
		assertEquals(id("forgero:metal"), second.tag());
	}

	@Test
	void testParseUnknownConditionType() {
		String json = """
				{
				  "type": "forgero:non_existent_predicate",
				  "value": "anything"
				}
				""";
		parseFailure(ConditionCodecs.CONDITION_DATA_CODEC, json, "Unknown predicate type: forgero:non_existent_predicate");
	}

	@Test
	void testParseInSlotTypePredicate() {
		String json = """
				{
				  "type": "forgero:in_slot_type",
				  "slot_type": "forgero:gem_slot"
				}
				""";
		InSlotTypePredicateData predicate = (InSlotTypePredicateData) parseSuccess(ConditionCodecs.CONDITION_DATA_CODEC, json).predicates().get(0);
		assertEquals(id("forgero:in_slot_type"), predicate.type());
		assertEquals(id("forgero:gem_slot"), predicate.slotType());
	}

	@Test
	void testParseInSlotTypePredicateMissingSlotType() {
		String json = "{ \"type\": \"forgero:in_slot_type\" }";
		parseFailure(ConditionCodecs.CONDITION_DATA_CODEC, json, "No key slot_type");
	}

	@Test
	void testParseSlotContainsPredicate() {
		String json = """
				{
				  "type": "forgero:slot_contains",
				  "slot": "handle",
				  "tag": "forgero:wooden_handle"
				}
				""";
		SlotContainsPredicateData predicate = (SlotContainsPredicateData) parseSuccess(ConditionCodecs.CONDITION_DATA_CODEC, json).predicates().get(0);
		assertEquals(id("forgero:slot_contains"), predicate.type());
		assertEquals("handle", predicate.slot());
		assertEquals(id("forgero:wooden_handle"), predicate.tag());
	}

	@Test
	void testParseSlotContainsPredicateMissingSlot() {
		String json = "{ \"type\": \"forgero:slot_contains\", \"tag\": \"forgero:wooden_handle\" }";
		parseFailure(ConditionCodecs.CONDITION_DATA_CODEC, json, "No key slot");
	}

	@Test
	void testParseAndPredicate() {
		String json = """
				{
				  "type": "forgero:and",
				  "predicates": [
				    { "type": "forgero:self_has_tag", "tag": "forgero:metal" },
				    { "type": "forgero:root_has_tag", "tag": "forgero:sword" }
				  ]
				}
				""";
		AndPredicateData predicate = (AndPredicateData) parseSuccess(ConditionCodecs.CONDITION_DATA_CODEC, json).predicates().get(0);
		assertEquals(id("forgero:and"), predicate.type());
		assertEquals(2, predicate.predicates().size());
	}

	@Test
	void testParseAndPredicateMissingPredicates() {
		String json = "{ \"type\": \"forgero:and\" }";
		parseFailure(ConditionCodecs.CONDITION_DATA_CODEC, json, "No key predicates");
	}

	@Test
	void testParseOrPredicate() {
		String json = """
				{
				  "type": "forgero:or",
				  "predicates": [
				    { "type": "forgero:self_has_tag", "tag": "forgero:axe" },
				    { "type": "forgero:in_slot_type", "slot_type": "forgero:head_slot" }
				  ]
				}
				""";
		OrPredicateData predicate = (OrPredicateData) parseSuccess(ConditionCodecs.CONDITION_DATA_CODEC, json).predicates().get(0);
		assertEquals(id("forgero:or"), predicate.type());
		assertEquals(2, predicate.predicates().size());
	}

	@Test
	void testParseNotPredicate() {
		String json = """
				{
				  "type": "forgero:not",
				  "predicate": { "type": "forgero:self_has_tag", "tag": "forgero:broken" }
				}
				""";
		NotPredicateData predicate = (NotPredicateData) parseSuccess(ConditionCodecs.CONDITION_DATA_CODEC, json).predicates().get(0);
		assertEquals(id("forgero:not"), predicate.type());
		assertNotNull(predicate.predicate());
		assertInstanceOf(TagMatchPredicateData.class, predicate.predicate());
	}

	@Test
	void testParseNotPredicateMissingChild() {
		String json = "{ \"type\": \"forgero:not\" }";
		parseFailure(ConditionCodecs.CONDITION_DATA_CODEC, json, "No key predicate");
	}
}
