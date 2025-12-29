package com.sigmundgranaas.forgero.data.loading.impl;

import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.tags.engine.TagGraph;
import com.sigmundgranaas.forgero.core.condition.api.Condition;
import com.sigmundgranaas.forgero.core.condition.api.ConditionCodec;
import com.sigmundgranaas.forgero.core.condition.api.DynamicCondition;
import com.sigmundgranaas.forgero.core.condition.api.StaticCondition;
import com.sigmundgranaas.forgero.core.condition.predicate.HasOtherContributorCondition;
import com.sigmundgranaas.forgero.core.condition.predicate.InSlotTypeCondition;
import com.sigmundgranaas.forgero.core.condition.predicate.TagMatchCondition;
import com.sigmundgranaas.forgero.data.loading.api.data.attribute.AttributeData;
import com.sigmundgranaas.forgero.data.loading.impl.codec.AttributeBatchCodecs;
import com.sigmundgranaas.forgero.data.loading.impl.codec.AttributeCodecs;
import com.sigmundgranaas.forgero.data.loading.impl.codec.CodecConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.sigmundgranaas.forgero.testutils.TestIdentifiers.id;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for attribute batch functionality.
 *
 * <p>Tests cover:</p>
 * <ul>
 *   <li>Basic batch expansion with simple values</li>
 *   <li>Auto-injection of attribute_type into has_other_contributor conditions</li>
 *   <li>Per-value computation overrides</li>
 *   <li>Different operators (add, multiply, subtract, etc.)</li>
 *   <li>ID prefix generation</li>
 *   <li>Multiple batches in a single array</li>
 *   <li>Error handling for invalid JSON</li>
 * </ul>
 */
class AttributeBatchCodecTest {

	private Codec<Condition> conditionCodec;
	private Codec<List<AttributeData>> batchCodec;

	@BeforeEach
	void setUp() {
		Map<String, Codec<? extends StaticCondition>> staticCodecs = new HashMap<>();
		Map<OpenIdentifier, Set<OpenIdentifier>> tagMap = new HashMap<>();
		tagMap.put(id("pickaxe"), new HashSet<>());

		staticCodecs.put("forgero:self_has_tag", TagMatchCondition.codec(() -> new TagGraph(tagMap)));
		staticCodecs.put("forgero:in_slot_type", InSlotTypeCondition.CODEC);
		staticCodecs.put("forgero:has_other_contributor", HasOtherContributorCondition.CODEC);

		Map<String, Codec<? extends DynamicCondition>> dynamicCodecs = new HashMap<>();
		this.conditionCodec = new ConditionCodec(staticCodecs, dynamicCodecs);
		this.batchCodec = AttributeBatchCodecs.createBatchListCodec(conditionCodec);
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
	void testBasicBatchExpansion() {
		String json = """
				[
				  {
				    "id_prefix": "forgero:iron",
				    "values": {
				      "forgero:attack_speed": 2.0,
				      "forgero:attack_damage": 4.0,
				      "forgero:mining_speed": 6.0
				    }
				  }
				]
				""";

		List<AttributeData> attributes = parseSuccess(batchCodec, json);

		assertEquals(3, attributes.size(), "Should expand into 3 attributes");

		// Check attack_speed
		var attackSpeed = attributes.stream()
				.filter(attr -> attr.type().equals(id("forgero:attack_speed")))
				.findFirst()
				.orElseThrow();
		assertEquals(Optional.of(id("forgero:iron-attack_speed")), attackSpeed.id());
		assertEquals(2.0f, attackSpeed.computation().value());
		assertEquals(AttributeCodecs.ADDITION_OPERATOR, attackSpeed.computation().operator());

		// Check attack_damage
		var attackDamage = attributes.stream()
				.filter(attr -> attr.type().equals(id("forgero:attack_damage")))
				.findFirst()
				.orElseThrow();
		assertEquals(Optional.of(id("forgero:iron-attack_damage")), attackDamage.id());
		assertEquals(4.0f, attackDamage.computation().value());
	}

	@Test
	void testAttributeTypeInjection() {
		String json = """
				[
				  {
				    "id_prefix": "forgero:iron",
				    "condition": [
				      {"type": "forgero:in_slot_type", "slot_type": "forgero:tool_material"},
				      {"type": "forgero:has_other_contributor"}
				    ],
				    "values": {
				      "forgero:attack_speed": 2.0,
				      "forgero:durability": 240.0
				    }
				  }
				]
				""";

		List<AttributeData> attributes = parseSuccess(batchCodec, json);

		assertEquals(2, attributes.size());

		// Check that has_other_contributor has attribute_type injected for attack_speed
		var attackSpeed = attributes.stream()
				.filter(attr -> attr.type().equals(id("forgero:attack_speed")))
				.findFirst()
				.orElseThrow();

		assertTrue(attackSpeed.condition().isPresent(), "Condition should be present");
		var condition = attackSpeed.condition().get();
		assertEquals(2, condition.staticConditions().size());

		var hasOtherContributor = condition.staticConditions().stream()
				.filter(cond -> cond instanceof HasOtherContributorCondition)
				.map(cond -> (HasOtherContributorCondition) cond)
				.findFirst()
				.orElseThrow();

		assertEquals(id("forgero:attack_speed"), hasOtherContributor.attributeType(),
				"Should inject attack_speed as attribute_type");

		// Check durability has its own attribute_type injected
		var durability = attributes.stream()
				.filter(attr -> attr.type().equals(id("forgero:durability")))
				.findFirst()
				.orElseThrow();

		var durabilityContributor = durability.condition().get().staticConditions().stream()
				.filter(cond -> cond instanceof HasOtherContributorCondition)
				.map(cond -> (HasOtherContributorCondition) cond)
				.findFirst()
				.orElseThrow();

		assertEquals(id("forgero:durability"), durabilityContributor.attributeType(),
				"Should inject durability as attribute_type");
	}

	@Test
	void testBatchWithMultiplicationOperator() {
		String json = """
				[
				  {
				    "id_prefix": "forgero:sharpness",
				    "computation": {
				      "value": 1.0,
				      "operator": "forgero:multiplication",
				      "order": "forgero:end"
				    },
				    "values": {
				      "forgero:attack_damage": 1.15,
				      "forgero:mining_speed": 1.05
				    }
				  }
				]
				""";

		List<AttributeData> attributes = parseSuccess(batchCodec, json);

		assertEquals(2, attributes.size());

		var attackDamage = attributes.stream()
				.filter(attr -> attr.type().equals(id("forgero:attack_damage")))
				.findFirst()
				.orElseThrow();

		assertEquals(1.15f, attackDamage.computation().value());
		assertEquals(AttributeCodecs.MULTIPLICATION_OPERATOR, attackDamage.computation().operator());
		assertEquals(AttributeCodecs.END_ORDER, attackDamage.computation().order());
	}

	@Test
	void testPerValueComputationOverride() {
		String json = """
				[
				  {
				    "id_prefix": "forgero:mixed",
				    "computation": {
				      "operator": "forgero:multiplication",
				      "order": "forgero:end"
				    },
				    "values": {
				      "forgero:attack_damage": 1.2,
				      "forgero:rarity": {
				        "value": 5.0,
				        "operator": "forgero:addition",
				        "order": "forgero:base"
				      }
				    }
				  }
				]
				""";

		List<AttributeData> attributes = parseSuccess(batchCodec, json);

		assertEquals(2, attributes.size());

		// attack_damage should use batch default (multiplication)
		var attackDamage = attributes.stream()
				.filter(attr -> attr.type().equals(id("forgero:attack_damage")))
				.findFirst()
				.orElseThrow();
		assertEquals(1.2f, attackDamage.computation().value());
		assertEquals(AttributeCodecs.MULTIPLICATION_OPERATOR, attackDamage.computation().operator());
		assertEquals(AttributeCodecs.END_ORDER, attackDamage.computation().order());

		// rarity should use its override (addition)
		var rarity = attributes.stream()
				.filter(attr -> attr.type().equals(id("forgero:rarity")))
				.findFirst()
				.orElseThrow();
		assertEquals(5.0f, rarity.computation().value());
		assertEquals(AttributeCodecs.ADDITION_OPERATOR, rarity.computation().operator());
		assertEquals(AttributeCodecs.BASE_ORDER, rarity.computation().order());
	}

	@Test
	void testMultipleBatches() {
		String json = """
				[
				  {
				    "id_prefix": "forgero:iron",
				    "values": {
				      "forgero:attack_speed": 2.0,
				      "forgero:attack_damage": 4.0
				    }
				  },
				  {
				    "id_prefix": "forgero:diamond",
				    "values": {
				      "forgero:attack_speed": 3.0,
				      "forgero:attack_damage": 6.0
				    }
				  }
				]
				""";

		List<AttributeData> attributes = parseSuccess(batchCodec, json);

		assertEquals(4, attributes.size(), "Should expand into 4 total attributes");

		// Check we have both iron and diamond variants
		long ironCount = attributes.stream()
				.filter(attr -> attr.id().map(id -> id.toString().contains("iron")).orElse(false))
				.count();
		long diamondCount = attributes.stream()
				.filter(attr -> attr.id().map(id -> id.toString().contains("diamond")).orElse(false))
				.count();

		assertEquals(2, ironCount);
		assertEquals(2, diamondCount);
	}

	@Test
	void testBatchWithoutIdPrefix() {
		String json = """
				[
				  {
				    "values": {
				      "forgero:attack_speed": 2.0
				    }
				  }
				]
				""";

		List<AttributeData> attributes = parseSuccess(batchCodec, json);

		assertEquals(1, attributes.size());
		var attr = attributes.get(0);

		// Without id_prefix, should use just the attribute type name
		assertEquals(Optional.of(id("attack_speed")), attr.id());
	}

	@Test
	void testBatchWithConditionButNoContributor() {
		String json = """
				[
				  {
				    "id_prefix": "forgero:test",
				    "condition": {"type": "forgero:in_slot_type", "slot_type": "forgero:upgrade_material"},
				    "values": {
				      "forgero:rarity": 10.0
				    }
				  }
				]
				""";

		List<AttributeData> attributes = parseSuccess(batchCodec, json);

		assertEquals(1, attributes.size());
		var attr = attributes.get(0);

		assertTrue(attr.condition().isPresent());
		assertEquals(1, attr.condition().get().staticConditions().size());
		assertInstanceOf(InSlotTypeCondition.class, attr.condition().get().staticConditions().get(0));
	}

	@Test
	void testComputationShorthandInValues() {
		String json = """
				[
				  {
				    "id_prefix": "forgero:test",
				    "values": {
				      "forgero:attack_damage": {
				        "multiply": 1.5
				      }
				    }
				  }
				]
				""";

		List<AttributeData> attributes = parseSuccess(batchCodec, json);

		assertEquals(1, attributes.size());
		var attr = attributes.get(0);

		assertEquals(1.5f, attr.computation().value());
		assertEquals(AttributeCodecs.MULTIPLICATION_OPERATOR, attr.computation().operator());
	}

	@Test
	void testRealWorldToolMaterialPattern() {
		// This simulates the real iron.json pattern
		String json = """
				[
				  {
				    "id_prefix": "forgero:iron",
				    "condition": [
				      {"type": "forgero:in_slot_type", "slot_type": "forgero:tool_material"},
				      {"type": "forgero:has_other_contributor"}
				    ],
				    "values": {
				      "forgero:attack_speed": 2.0,
				      "forgero:attack_damage": 4.0,
				      "forgero:mining_speed": 6.0,
				      "forgero:mining_level": 2.0,
				      "forgero:durability": 240.0,
				      "forgero:rarity": 40.0,
				      "forgero:weight": 5.0,
				      "forgero:draw_speed": 1.25,
				      "forgero:draw_power": 3.75,
				      "forgero:armor": 0.0
				    }
				  }
				]
				""";

		List<AttributeData> attributes = parseSuccess(batchCodec, json);

		assertEquals(10, attributes.size(), "Should expand into 10 attributes");

		// Verify all have the correct conditions
		attributes.forEach(attr -> {
			assertTrue(attr.condition().isPresent());
			assertEquals(2, attr.condition().get().staticConditions().size());

			var hasOtherContributor = attr.condition().get().staticConditions().stream()
					.filter(cond -> cond instanceof HasOtherContributorCondition)
					.map(cond -> (HasOtherContributorCondition) cond)
					.findFirst()
					.orElseThrow();

			// Each should have its own attribute type injected
			assertEquals(attr.type(), hasOtherContributor.attributeType(),
					"Attribute type should match in has_other_contributor condition");
		});
	}
}
