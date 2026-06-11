package com.sigmundgranaas.forgero.core.property.attribute.impl;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.tags.engine.TagGraph;
import com.sigmundgranaas.forgero.core.attribute.api.AttributeQueryResult;
import com.sigmundgranaas.forgero.core.attribute.impl.AttributeEngine;
import com.sigmundgranaas.forgero.core.condition.api.Condition;
import com.sigmundgranaas.forgero.core.condition.api.DynamicCondition;
import com.sigmundgranaas.forgero.core.condition.api.StaticCondition;
import com.sigmundgranaas.forgero.core.attribute.api.PrecomputedAttribute;
import com.sigmundgranaas.forgero.core.condition.predicate.SlotContainsCondition;
import com.sigmundgranaas.forgero.core.condition.predicate.TagMatchCondition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.sigmundgranaas.forgero.core.attribute.api.DefaultAttributes.*;
import static com.sigmundgranaas.forgero.testutils.ForgeroTestFactory.*;
import static com.sigmundgranaas.forgero.testutils.TestIdentifiers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Validates the core functionality of the {@link AttributeEngine} and its interaction
 * with the new {@link AttributeQueryResult}.
 */
class AttributeResolverTest {

	/**
	 * Tests if the resolver can correctly resolve attributes and that the query result
	 * can provide separate values.
	 */
	@Test
	void resolvesAndCalculatesCorrectly() {
		var head = part(PICKAXE_HEAD_ID).withTag(METAL_TAG).withTag(HEAD_SLOT_TYPE).withAttribute(ATTACK_DAMAGE, 10f).build();
		var handle = part(HANDLE_ID).withTag(WOOD_TAG).withTag(HANDLE_SLOT_TYPE).withAttribute(MINING_SPEED, 5f).build();
		var pickaxe = tool(PICKAXE_ID).withTag("tool").withAttribute(ATTACK_DAMAGE, 1f)
				.withPart(head, "head_slot", HEAD_SLOT_TYPE)
				.withPart(handle, "handle_slot", HANDLE_SLOT_TYPE)
				.build();

		AttributeQueryResult result = resolveAttributes(pickaxe);

		float attackDamage = result.getValue(ATTACK_DAMAGE);
		assertEquals(11f, attackDamage, "Should be 10 from head + 1 from pickaxe base.");

		float miningSpeed = result.getValue(MINING_SPEED);
		assertEquals(5f, miningSpeed, "Should be 5 from handle.");
	}


	/**
	 * Tests a complex scenario involving both static (structural) and dynamic conditions.
	 * Static conditions are resolved during compilation; dynamic conditions never affect
	 * the compiled value and are carried through as data for the game layer.
	 */
	@Test
	void appliesStaticAndDynamicConditions() {
		// DYNAMIC CONDITION: opaque data for the game layer; core never evaluates it.
		DynamicCondition onStone = () -> OpenIdentifier.parse("forgero:none");

		var diamondCondition = new Condition(Collections.emptyList(), List.of(onStone));
		var diamondProperty = attribute(MINING_SPEED).withValue(10f).withCondition(diamondCondition).build();
		var diamond = part(DIAMOND_ID).withTag(GEM_TAG).withTag(GEM_SLOT_TYPE).withAttribute(diamondProperty).build();

		Map<OpenIdentifier, Set<OpenIdentifier>> tagMap = new HashMap<>();
		tagMap.put(id("pickaxe"), new HashSet<>());

		// STATIC CONDITION: Active only if the root item is a 'pickaxe'.
		StaticCondition rootIsPickaxe = new TagMatchCondition(id("forgero:root_has_tag"), id("pickaxe"), () -> new TagGraph(tagMap));
		var ironCondition = new Condition(List.of(rootIsPickaxe), Collections.emptyList());
		var ironProperty = attribute(MINING_SPEED).withValue(5f).withCondition(ironCondition).build();
		var iron = part(IRON_ID).withTag(METAL_TAG).withTag(MATERIAL_SLOT_TYPE).withAttribute(ironProperty).build();


		var head = part(PICKAXE_HEAD_ID)
				.withTag(HEAD_SLOT_TYPE).withTag(id("blade"))
				.withStructureSlot(structureSlot("material_slot", MATERIAL_SLOT_TYPE, iron))
				.withStructureSlot(structureSlot("gem_slot", GEM_SLOT_TYPE, diamond))
				.build();

		var pickaxe = tool(PICKAXE_ID).withTag("pickaxe").withTag("tool").withAttribute(MINING_SPEED, 1f)
				.withPart(head, "head_slot", HEAD_SLOT_TYPE)
				.build();

		var sword = tool(SWORD_ID).withTag("sword").withTag("tool").withAttribute(MINING_SPEED, 1f)
				.withPart(head, "blade_slot", id("blade"))
				.build();

		// Compiled values: dynamic-conditional attributes never contribute.
		AttributeQueryResult pickaxeResult = resolveAttributes(pickaxe);
		assertEquals(6f, pickaxeResult.getValue(MINING_SPEED), "Base (1) + Iron (5) = 6. Diamond bonus is dynamic and carried as data.");

		AttributeQueryResult swordResult = resolveAttributes(sword);
		assertEquals(1f, swordResult.getValue(MINING_SPEED), "Base (1) only. Iron bonus statically inactive, diamond bonus dynamic.");

		// The dynamic-conditional diamond bonus is carried as data on the baked result.
		PrecomputedAttribute pickaxeSpeed = attributeEngine().resolve(pickaxe).get(MINING_SPEED);
		assertEquals(6f, pickaxeSpeed.value(), "Compiled value excludes the dynamic diamond bonus.");
		assertEquals(1, pickaxeSpeed.conditionalAttributes().size(), "Dynamic diamond bonus is carried as data.");
		assertEquals(10f, pickaxeSpeed.conditionalAttributes().get(0).value(), "Carried attribute keeps its value.");

		PrecomputedAttribute swordSpeed = attributeEngine().resolve(sword).get(MINING_SPEED);
		assertEquals(1f, swordSpeed.value(), "Compiled value excludes statically-failed iron and dynamic diamond bonuses.");
		assertEquals(1, swordSpeed.conditionalAttributes().size(), "Dynamic diamond bonus is carried as data.");
	}

	/**
	 * Tests a condition that depends on the state of a sibling component.
	 */
	@Test
	void appliesComplexStructuralConditions() {
		// Condition: bonus is active if the slot of type HANDLE_SLOT_TYPE contains a component with the "wood" tag.
		StaticCondition handleContainsWood = new SlotContainsCondition(id("forgero:slot_contains"), id("forgero:handle"), id("forgero:wood"));
		var headBonusCondition = new Condition(List.of(handleContainsWood), Collections.emptyList());
		var headBonus = attribute(ATTACK_DAMAGE).withValue(5).withCondition(headBonusCondition).build();

		var head = part(PICKAXE_HEAD_ID).withTag(METAL_TAG).withTag(HEAD_SLOT_TYPE).withAttribute(headBonus).build();
		var oakHandle = part(HANDLE_ID).withTag(WOOD_TAG).withTag(HANDLE_SLOT_TYPE).build();
		var ironHandle = part(HANDLE_ID).withTag(METAL_TAG).withTag(HANDLE_SLOT_TYPE).build();

		var woodPickaxe = tool(PICKAXE_ID).withAttribute(ATTACK_DAMAGE, 1f)
				.withPart(head, "head_slot", HEAD_SLOT_TYPE)
				.withPart(oakHandle, "handle_slot", HANDLE_SLOT_TYPE)
				.build();

		var ironPickaxe = tool(PICKAXE_ID).withAttribute(ATTACK_DAMAGE, 1f)
				.withPart(head, "head_slot", HEAD_SLOT_TYPE)
				.withPart(ironHandle, "handle_slot", HANDLE_SLOT_TYPE)
				.build();

		float woodDamage = resolveAttributes(woodPickaxe).getValue(ATTACK_DAMAGE);
		assertEquals(6f, woodDamage, "Base damage (1) + head bonus (5, because handle is wood) = 6");

		float ironDamage = resolveAttributes(ironPickaxe).getValue(ATTACK_DAMAGE);
		assertEquals(1f, ironDamage, "Base damage (1) only, because handle is not wood.");
	}
}
