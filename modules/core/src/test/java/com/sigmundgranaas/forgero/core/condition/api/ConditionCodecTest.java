package com.sigmundgranaas.forgero.core.condition.api;

import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.tags.engine.TagGraph;
import com.sigmundgranaas.forgero.core.ForgeroTest;
import com.sigmundgranaas.forgero.core.condition.predicate.HasOtherContributorCondition;
import com.sigmundgranaas.forgero.core.condition.predicate.InSlotTypeCondition;
import com.sigmundgranaas.forgero.core.condition.predicate.TagMatchCondition;
import com.sigmundgranaas.forgero.data.loading.impl.codec.CodecConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for ConditionCodec parsing.
 *
 * <p>These tests verify that conditions are correctly parsed from JSON in all supported formats:</p>
 * <ul>
 *   <li>Wrapper format: {@code { "static": [...], "dynamic": [...] }}</li>
 *   <li>Legacy list format: {@code [ {...}, {...} ]}</li>
 *   <li>Single predicate format: {@code { "type": "..." }}</li>
 * </ul>
 *
 * <p>This is critical because a bug in condition parsing caused ALL conditions to be silently
 * dropped, resulting in armor attributes leaking into tools.</p>
 */
@DisplayName("ConditionCodec Tests")
class ConditionCodecTest extends ForgeroTest {

	private ConditionCodec conditionCodec;

	@BeforeEach
	void setUp() {
		Map<String, Codec<? extends StaticCondition>> staticCodecs = new HashMap<>();
		Map<OpenIdentifier, Set<OpenIdentifier>> tagMap = new HashMap<>();
		tagMap.put(id("forgero:pickaxe"), new HashSet<>());
		tagMap.put(id("forgero:tool"), new HashSet<>());

		staticCodecs.put("forgero:in_slot_type", InSlotTypeCondition.CODEC);
		staticCodecs.put("forgero:has_other_contributor", HasOtherContributorCondition.CODEC);
		staticCodecs.put("forgero:self_has_tag", TagMatchCondition.codec(() -> new TagGraph(tagMap)));

		Map<String, Codec<? extends DynamicCondition>> dynamicCodecs = new HashMap<>();
		this.conditionCodec = new ConditionCodec(staticCodecs, dynamicCodecs);
	}

	private OpenIdentifier id(String id) {
		return CodecConstants.IDENTIFIER_FACTORY.of(id);
	}

	private Condition parseSuccess(String json) {
		DataResult<Condition> result = conditionCodec.parse(JsonOps.INSTANCE, JsonParser.parseString(json));
		assertTrue(result.result().isPresent(),
				"Parsing should succeed. Error: " + result.error().map(DataResult.PartialResult::message).orElse("No error message"));
		return result.result().get();
	}

	// ==================== WRAPPER FORMAT TESTS ====================

	@Nested
	@DisplayName("Wrapper format: { \"static\": [...], \"dynamic\": [...] }")
	class WrapperFormatTests {

		/**
		 * CRITICAL TEST: This is the format used in actual material JSON files.
		 * A bug where this format wasn't parsed caused all conditions to be dropped.
		 */
		@Test
		@DisplayName("Parses single static condition in wrapper format")
		void parsesSingleStaticConditionInWrapperFormat() {
			String json = """
					{
					  "static": [
					    { "type": "forgero:in_slot_type", "slot_type": "forgero:armor_material" }
					  ]
					}
					""";

			Condition condition = parseSuccess(json);

			assertEquals(1, condition.staticConditions().size(),
					"Should parse exactly one static condition");
			assertTrue(condition.dynamicConditions().isEmpty(),
					"Should have no dynamic conditions");

			StaticCondition predicate = condition.staticConditions().get(0);
			assertInstanceOf(InSlotTypeCondition.class, predicate);
			InSlotTypeCondition slotCondition = (InSlotTypeCondition) predicate;
			assertEquals(id("forgero:armor_material"), slotCondition.slotType());
		}

		/**
		 * Tests the iron.json pattern with multiple static conditions.
		 */
		@Test
		@DisplayName("Parses multiple static conditions in wrapper format (iron.json pattern)")
		void parsesMultipleStaticConditionsInWrapperFormat() {
			String json = """
					{
					  "static": [
					    { "type": "forgero:in_slot_type", "slot_type": "forgero:tool_material" },
					    { "type": "forgero:has_other_contributor", "attribute_type": "forgero:attack_damage" }
					  ]
					}
					""";

			Condition condition = parseSuccess(json);

			assertEquals(2, condition.staticConditions().size(),
					"Should parse both static conditions");

			// Verify first condition
			assertInstanceOf(InSlotTypeCondition.class, condition.staticConditions().get(0));
			InSlotTypeCondition slotCondition = (InSlotTypeCondition) condition.staticConditions().get(0);
			assertEquals(id("forgero:tool_material"), slotCondition.slotType());

			// Verify second condition
			assertInstanceOf(HasOtherContributorCondition.class, condition.staticConditions().get(1));
			HasOtherContributorCondition contributorCondition = (HasOtherContributorCondition) condition.staticConditions().get(1);
			assertEquals(id("forgero:attack_damage"), contributorCondition.attributeType());
		}

		@Test
		@DisplayName("Parses empty wrapper format (no conditions)")
		void parsesEmptyWrapperFormat() {
			String json = """
					{
					  "static": [],
					  "dynamic": []
					}
					""";

			Condition condition = parseSuccess(json);

			assertTrue(condition.staticConditions().isEmpty());
			assertTrue(condition.dynamicConditions().isEmpty());
		}

		@Test
		@DisplayName("Parses wrapper format with only static key")
		void parsesWrapperFormatWithOnlyStaticKey() {
			String json = """
					{
					  "static": [
					    { "type": "forgero:in_slot_type", "slot_type": "forgero:tool_material" }
					  ]
					}
					""";

			Condition condition = parseSuccess(json);

			assertEquals(1, condition.staticConditions().size());
			assertTrue(condition.dynamicConditions().isEmpty());
		}

		@Test
		@DisplayName("Parses wrapper format with only dynamic key")
		void parsesWrapperFormatWithOnlyDynamicKey() {
			// Note: We don't have any dynamic condition codecs registered,
			// so this tests that the format is accepted but no conditions are parsed
			String json = """
					{
					  "dynamic": []
					}
					""";

			Condition condition = parseSuccess(json);

			assertTrue(condition.staticConditions().isEmpty());
			assertTrue(condition.dynamicConditions().isEmpty());
		}
	}

	// ==================== LEGACY FORMAT TESTS ====================

	@Nested
	@DisplayName("Legacy list format: [ {...}, {...} ]")
	class LegacyListFormatTests {

		@Test
		@DisplayName("Parses single predicate in list")
		void parsesSinglePredicateInList() {
			String json = """
					[
					  { "type": "forgero:in_slot_type", "slot_type": "forgero:tool_material" }
					]
					""";

			Condition condition = parseSuccess(json);

			assertEquals(1, condition.staticConditions().size());
		}

		@Test
		@DisplayName("Parses multiple predicates in list")
		void parsesMultiplePredicatesInList() {
			String json = """
					[
					  { "type": "forgero:in_slot_type", "slot_type": "forgero:tool_material" },
					  { "type": "forgero:has_other_contributor", "attribute_type": "forgero:durability" }
					]
					""";

			Condition condition = parseSuccess(json);

			assertEquals(2, condition.staticConditions().size());
		}
	}

	// ==================== SINGLE PREDICATE FORMAT TESTS ====================

	@Nested
	@DisplayName("Single predicate format: { \"type\": \"...\" }")
	class SinglePredicateFormatTests {

		@Test
		@DisplayName("Parses single predicate object")
		void parsesSinglePredicateObject() {
			String json = """
					{ "type": "forgero:in_slot_type", "slot_type": "forgero:armor_material" }
					""";

			Condition condition = parseSuccess(json);

			assertEquals(1, condition.staticConditions().size());
			assertInstanceOf(InSlotTypeCondition.class, condition.staticConditions().get(0));
		}
	}

	// ==================== ERROR HANDLING TESTS ====================

	@Nested
	@DisplayName("Error handling and edge cases")
	class ErrorHandlingTests {

		@Test
		@DisplayName("Unknown condition type is logged but doesn't crash")
		void unknownConditionTypeDoesNotCrash() {
			String json = """
					{
					  "static": [
					    { "type": "forgero:unknown_type", "some_field": "value" }
					  ]
					}
					""";

			// This should not crash, but should log an error
			Condition condition = parseSuccess(json);

			// The unknown condition is skipped, so we should have no conditions
			assertTrue(condition.staticConditions().isEmpty(),
					"Unknown condition types should be skipped");
		}

		@Test
		@DisplayName("Missing type field is handled gracefully")
		void missingTypeFieldHandledGracefully() {
			String json = """
					{
					  "static": [
					    { "slot_type": "forgero:tool_material" }
					  ]
					}
					""";

			// Should not crash
			Condition condition = parseSuccess(json);

			// The invalid predicate is skipped
			assertTrue(condition.staticConditions().isEmpty());
		}
	}

	// ==================== REAL-WORLD PATTERN TESTS ====================

	@Nested
	@DisplayName("Real-world JSON patterns from material files")
	class RealWorldPatternTests {

		/**
		 * This test mirrors the exact JSON structure used in iron.json for attack_damage.
		 */
		@Test
		@DisplayName("Parses iron.json attack_damage condition pattern")
		void parsesIronAttackDamageCondition() {
			String json = """
					{
					  "static": [
					    {
					      "type": "forgero:in_slot_type",
					      "slot_type": "forgero:tool_material"
					    },
					    {
					      "type": "forgero:has_other_contributor",
					      "attribute_type": "forgero:attack_damage"
					    }
					  ]
					}
					""";

			Condition condition = parseSuccess(json);

			assertEquals(2, condition.staticConditions().size(),
					"Iron attack_damage should have 2 conditions: in_slot_type AND has_other_contributor");

			// Both conditions must be present for attributes to apply correctly
			boolean hasSlotCondition = condition.staticConditions().stream()
					.anyMatch(c -> c instanceof InSlotTypeCondition);
			boolean hasContributorCondition = condition.staticConditions().stream()
					.anyMatch(c -> c instanceof HasOtherContributorCondition);

			assertTrue(hasSlotCondition, "Should have InSlotTypeCondition");
			assertTrue(hasContributorCondition, "Should have HasOtherContributorCondition");
		}

		/**
		 * This test mirrors the exact JSON structure used in iron.json for armor.
		 */
		@Test
		@DisplayName("Parses iron.json armor condition pattern")
		void parsesIronArmorCondition() {
			String json = """
					{
					  "static": [
					    {
					      "type": "forgero:in_slot_type",
					      "slot_type": "forgero:armor_material"
					    },
					    {
					      "type": "forgero:has_other_contributor",
					      "attribute_type": "forgero:armor"
					    }
					  ]
					}
					""";

			Condition condition = parseSuccess(json);

			assertEquals(2, condition.staticConditions().size(),
					"Iron armor should have 2 conditions");

			// Verify the slot condition points to armor_material
			InSlotTypeCondition slotCondition = condition.staticConditions().stream()
					.filter(c -> c instanceof InSlotTypeCondition)
					.map(c -> (InSlotTypeCondition) c)
					.findFirst()
					.orElseThrow();

			assertEquals(id("forgero:armor_material"), slotCondition.slotType(),
					"Armor attribute should only apply in armor_material slot");
		}

		/**
		 * Tests that the TagMatchCondition can be parsed in wrapper format.
		 */
		@Test
		@DisplayName("Parses self_has_tag condition in wrapper format")
		void parsesSelfHasTagCondition() {
			String json = """
					{
					  "static": [
					    { "type": "forgero:self_has_tag", "tag": "forgero:pickaxe" }
					  ]
					}
					""";

			Condition condition = parseSuccess(json);

			assertEquals(1, condition.staticConditions().size());
			assertInstanceOf(TagMatchCondition.class, condition.staticConditions().get(0));
			TagMatchCondition tagCondition = (TagMatchCondition) condition.staticConditions().get(0);
			assertEquals(id("forgero:pickaxe"), tagCondition.tag());
		}
	}
}
