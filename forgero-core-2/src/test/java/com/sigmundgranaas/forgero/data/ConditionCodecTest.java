package com.sigmundgranaas.forgero.data;

import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.sigmundgranaas.forgero.data.v3.codec.ConditionCodecs;
import com.sigmundgranaas.forgero.data.v3.codec.CodecConstants; // Added import
import com.sigmundgranaas.forgero.data.v3.dto.condition.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConditionCodecTest {

	@Test
	void testParseSingleConditionObject() {
		String json = """
				{
				  "type": "forgero:self_has_tag",
				  "tag": "forgero:gem"
				}
				""";
		var result = ConditionCodecs.CONDITION_DATA_CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json));
		assertTrue(result.result().isPresent(), "Parsing a single condition object should succeed. " + result.error().map(DataResult.PartialResult::message).orElse(""));

		ConditionData data = result.result().get();
		assertNotNull(data);
		assertEquals(1, data.predicates().size());
		assertTrue(data.predicates().get(0) instanceof TagMatchPredicateData, "Predicate should be a TagMatchPredicateData");

		TagMatchPredicateData predicate = (TagMatchPredicateData) data.predicates().get(0);
		assertEquals(CodecConstants.IDENTIFIER_FACTORY.of("forgero:self_has_tag"), predicate.type()); // Changed to OpenIdentifier
		assertEquals(CodecConstants.IDENTIFIER_FACTORY.of("forgero:gem"), predicate.tag()); // Changed to OpenIdentifier
	}

	@Test
	void testParseConditionArray() {
		String json = """
				[
				  {
				    "type": "forgero:root_has_tag",
				    "tag": "forgero:pickaxe"
				  },
				  {
				    "type": "forgero:self_has_tag",
				    "tag": "forgero:metal"
				  }
				]
				""";
		var result = ConditionCodecs.CONDITION_DATA_CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json));
		assertTrue(result.result().isPresent(), "Parsing a condition array should succeed. " + result.error().map(DataResult.PartialResult::message).orElse(""));

		ConditionData data = result.result().get();
		assertNotNull(data);
		assertEquals(2, data.predicates().size());

		assertTrue(data.predicates().get(0) instanceof TagMatchPredicateData);
		TagMatchPredicateData first = (TagMatchPredicateData) data.predicates().get(0);
		assertEquals(CodecConstants.IDENTIFIER_FACTORY.of("forgero:root_has_tag"), first.type()); // Changed to OpenIdentifier
		assertEquals(CodecConstants.IDENTIFIER_FACTORY.of("forgero:pickaxe"), first.tag()); // Changed to OpenIdentifier

		assertTrue(data.predicates().get(1) instanceof TagMatchPredicateData);
		TagMatchPredicateData second = (TagMatchPredicateData) data.predicates().get(1);
		assertEquals(CodecConstants.IDENTIFIER_FACTORY.of("forgero:self_has_tag"), second.type()); // Changed to OpenIdentifier
		assertEquals(CodecConstants.IDENTIFIER_FACTORY.of("forgero:metal"), second.tag()); // Changed to OpenIdentifier
	}

	@Test
	void testParseUnknownConditionType() {
		String json = """
				{
				  "type": "forgero:non_existent_predicate",
				  "value": "anything"
				}
				""";
		var result = ConditionCodecs.CONDITION_DATA_CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json));
		assertTrue(result.result().isEmpty(), "Parsing an unknown condition type should fail.");
		assertTrue(result.error().isPresent(), "An error message should be present.");
		assertTrue(result.error().get().message().contains("Unknown predicate type: forgero:non_existent_predicate"), result.error().get().message());
	}

	@Test
	void testParseInSlotTypePredicate() {
		String json = """
				{
				  "type": "forgero:in_slot_type",
				  "slot_type": "forgero:gem_slot"
				}
				""";
		var result = ConditionCodecs.CONDITION_DATA_CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json));
		assertTrue(result.result().isPresent(), "Parsing InSlotTypePredicate should succeed. " + result.error().map(DataResult.PartialResult::message).orElse(""));
		InSlotTypePredicateData predicate = (InSlotTypePredicateData) result.result().get().predicates().get(0);
		assertEquals(CodecConstants.IDENTIFIER_FACTORY.of("forgero:in_slot_type"), predicate.type()); // Changed to OpenIdentifier
		assertEquals(CodecConstants.IDENTIFIER_FACTORY.of("forgero:gem_slot"), predicate.slotType()); // Changed to OpenIdentifier
	}

	@Test
	void testParseInSlotTypePredicateMissingSlotType() {
		String json = """
				{
				  "type": "forgero:in_slot_type"
				}
				""";
		var result = ConditionCodecs.CONDITION_DATA_CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json));
		assertTrue(result.result().isEmpty(), "Parsing InSlotTypePredicate missing slot_type should fail.");
		assertTrue(result.error().isPresent());
		assertTrue(result.error().get().message().contains("No key slot_type"), result.error().get().message());
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
		var result = ConditionCodecs.CONDITION_DATA_CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json));
		assertTrue(result.result().isPresent(), "Parsing SlotContainsPredicate should succeed. " + result.error().map(DataResult.PartialResult::message).orElse(""));
		SlotContainsPredicateData predicate = (SlotContainsPredicateData) result.result().get().predicates().get(0);
		assertEquals(CodecConstants.IDENTIFIER_FACTORY.of("forgero:slot_contains"), predicate.type()); // Changed to OpenIdentifier
		assertEquals("handle", predicate.slot()); // Remains String
		assertEquals(CodecConstants.IDENTIFIER_FACTORY.of("forgero:wooden_handle"), predicate.tag()); // Changed to OpenIdentifier
	}

	@Test
	void testParseSlotContainsPredicateMissingSlot() {
		String json = """
				{
				  "type": "forgero:slot_contains",
				  "tag": "forgero:wooden_handle"
				}
				""";
		var result = ConditionCodecs.CONDITION_DATA_CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json));
		assertTrue(result.result().isEmpty(), "Parsing SlotContainsPredicate missing slot should fail.");
		assertTrue(result.error().isPresent());
		assertTrue(result.error().get().message().contains("No key slot"), result.error().get().message());
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
		var result = ConditionCodecs.CONDITION_DATA_CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json));
		assertTrue(result.result().isPresent(), "Parsing AndPredicate should succeed. " + result.error().map(DataResult.PartialResult::message).orElse(""));
		AndPredicateData predicate = (AndPredicateData) result.result().get().predicates().get(0);
		assertEquals(CodecConstants.IDENTIFIER_FACTORY.of("forgero:and"), predicate.type()); // Changed to OpenIdentifier
		assertEquals(2, predicate.predicates().size());
		assertTrue(predicate.predicates().get(0) instanceof TagMatchPredicateData);
		assertTrue(predicate.predicates().get(1) instanceof TagMatchPredicateData);
	}

	@Test
	void testParseAndPredicateEmptyPredicates() {
		String json = """
				{
				  "type": "forgero:and",
				  "predicates": []
				}
				""";
		var result = ConditionCodecs.CONDITION_DATA_CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json));
		assertTrue(result.result().isPresent(), "Parsing AndPredicate with empty list should succeed.");
		AndPredicateData predicate = (AndPredicateData) result.result().get().predicates().get(0);
		assertEquals(0, predicate.predicates().size());
	}

	@Test
	void testParseAndPredicateMissingPredicates() {
		String json = """
				{
				  "type": "forgero:and"
				}
				""";
		var result = ConditionCodecs.CONDITION_DATA_CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json));
		assertTrue(result.result().isEmpty(), "Parsing AndPredicate missing predicates should fail.");
		assertTrue(result.error().isPresent());
		assertTrue(result.error().get().message().contains("No key predicates"), result.error().get().message());
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
		var result = ConditionCodecs.CONDITION_DATA_CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json));
		assertTrue(result.result().isPresent(), "Parsing OrPredicate should succeed. " + result.error().map(DataResult.PartialResult::message).orElse(""));
		OrPredicateData predicate = (OrPredicateData) result.result().get().predicates().get(0);
		assertEquals(CodecConstants.IDENTIFIER_FACTORY.of("forgero:or"), predicate.type()); // Changed to OpenIdentifier
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
		var result = ConditionCodecs.CONDITION_DATA_CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json));
		assertTrue(result.result().isPresent(), "Parsing NotPredicate should succeed. " + result.error().map(DataResult.PartialResult::message).orElse(""));
		NotPredicateData predicate = (NotPredicateData) result.result().get().predicates().get(0);
		assertEquals(CodecConstants.IDENTIFIER_FACTORY.of("forgero:not"), predicate.type()); // Changed to OpenIdentifier
		assertNotNull(predicate.predicate());
		assertTrue(predicate.predicate() instanceof TagMatchPredicateData);
	}

	@Test
	void testParseNotPredicateMissingChild() {
		String json = """
				{
				  "type": "forgero:not"
				}
				""";
		var result = ConditionCodecs.CONDITION_DATA_CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json));
		assertTrue(result.result().isEmpty(), "Parsing NotPredicate missing child predicate should fail.");
		assertTrue(result.error().isPresent());
		assertTrue(result.error().get().message().contains("No key predicate"), result.error().get().message());
	}
}
