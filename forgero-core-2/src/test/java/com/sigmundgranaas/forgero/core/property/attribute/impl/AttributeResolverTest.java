package com.sigmundgranaas.forgero.core.property.attribute.impl;

import com.sigmundgranaas.forgero.core.attribute.api.AttributeQueryResult;
import com.sigmundgranaas.forgero.core.attribute.impl.AttributeEngine;
import com.sigmundgranaas.forgero.core.property.api.Resolver;
import com.sigmundgranaas.forgero.core.property.condition.Condition;
import com.sigmundgranaas.forgero.core.property.condition.DynamicCondition;
import com.sigmundgranaas.forgero.core.property.condition.StaticConditions;
import com.sigmundgranaas.forgero.core.property.context.ContextKeys;
import com.sigmundgranaas.forgero.core.property.context.DynamicContext;
import com.sigmundgranaas.forgero.core.property.engine.ResolverEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.sigmundgranaas.forgero.core.attribute.api.DefaultAttributes.*;
import static com.sigmundgranaas.forgero.testutils.ForgeroTestFactory.*;
import static com.sigmundgranaas.forgero.testutils.TestIdentifiers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Validates the core functionality of the {@link AttributeEngine} and its interaction
 * with the new {@link Resolver} and {@link AttributeQueryResult}.
 */
class AttributeResolverTest {
	private Resolver resolver;

	@BeforeEach
	void setUp() {
		resolver = new ResolverEngine();
	}

	/**
	 * Tests if the resolver can correctly resolve attributes and that the query result
	 * can provide separate values.
	 */
	@Test
	void resolvesAndCalculatesCorrectly() {
		var head = part(PICKAXE_HEAD_ID).withTag(METAL_TAG).withAttribute(ATTACK_DAMAGE, 10f).build();
		var handle = part(HANDLE_ID).withTag(WOOD_TAG).withAttribute(MINING_SPEED, 5f).build();
		var pickaxe = tool(PICKAXE_ID).withTag("tool").withAttribute(ATTACK_DAMAGE, 1f)
				.withPart(head, "head_slot", HEAD_SLOT_TYPE)
				.withPart(handle, "handle_slot", HANDLE_SLOT_TYPE)
				.build();

		Optional<AttributeQueryResult> resultOpt = resolver.resolve(pickaxe, AttributeEngine.KEY);
		assertTrue(resultOpt.isPresent());
		AttributeQueryResult result = resultOpt.get();

		float attackDamage = result.getValue(ATTACK_DAMAGE);
		assertEquals(11f, attackDamage, "Should be 10 from head + 1 from pickaxe base.");

		float miningSpeed = result.getValue(MINING_SPEED);
		assertEquals(5f, miningSpeed, "Should be 5 from handle.");
	}


	/**
	 * Tests a complex scenario involving both static (structural) and dynamic (contextual) conditions.
	 */
	@Test
	void appliesStaticAndDynamicConditions() {
		// DYNAMIC CONDITION: Active only if the target has the 'stone' tag.
		DynamicCondition onStone = (ctx) -> ctx.get(ContextKeys.TARGET_TAGS)
				.map(tags -> tags.contains(id("stone")))
				.orElse(false);
		var diamondCondition = new Condition(Collections.emptyList(), List.of(onStone));
		var diamondProperty = attribute(MINING_SPEED).withValue(10f).withCondition(diamondCondition).build();
		var diamond = part(DIAMOND_ID).withTag(GEM_TAG).withProperty(diamondProperty).build();

		// STATIC CONDITION: Active only if the root item is a 'pickaxe'.
		var ironCondition = new Condition(List.of(StaticConditions.rootHasTag("pickaxe")), Collections.emptyList());
		var ironProperty = attribute(MINING_SPEED).withValue(5f).withCondition(ironCondition).build();
		var iron = part(IRON_ID).withTag(METAL_TAG).withProperty(ironProperty).build();


		var head = part(PICKAXE_HEAD_ID)
				.withStructureSlot(structureSlot("material_slot", MATERIAL_SLOT_TYPE, iron))
				.withStructureSlot(structureSlot("gem_slot", GEM_SLOT_TYPE, diamond))
				.build();

		var pickaxe = tool(PICKAXE_ID).withTag("pickaxe").withTag("tool").withAttribute(MINING_SPEED, 1f)
				.withPart(head, "head_slot", HEAD_SLOT_TYPE)
				.build();

		var sword = tool(SWORD_ID).withTag("sword").withTag("tool").withAttribute(MINING_SPEED, 1f)
				.withPart(head, "blade_slot", id("blade"))
				.build();

		DynamicContext stoneTarget = new DynamicContext.Builder().put(ContextKeys.TARGET_TAGS, Set.of(id("stone"))).build();
		DynamicContext woodTarget = new DynamicContext.Builder().put(ContextKeys.TARGET_TAGS, Set.of(id("wood"))).build();

		// Test pickaxe against different contexts
		AttributeQueryResult pickaxeStoneResult = resolver.resolve(pickaxe, AttributeEngine.KEY, stoneTarget).orElseThrow();
		assertEquals(16f, pickaxeStoneResult.getValue(MINING_SPEED), "Base (1) + Iron (5) + Diamond (10) = 16");

		AttributeQueryResult pickaxeWoodResult = resolver.resolve(pickaxe, AttributeEngine.KEY, woodTarget).orElseThrow();
		assertEquals(6f, pickaxeWoodResult.getValue(MINING_SPEED), "Base (1) + Iron (5) = 6");

		// Test sword against different contexts
		AttributeQueryResult swordStoneResult = resolver.resolve(sword, AttributeEngine.KEY, stoneTarget).orElseThrow();
		assertEquals(11f, swordStoneResult.getValue(MINING_SPEED), "Base (1) + Diamond (10) = 11. Iron bonus inactive.");

		AttributeQueryResult swordWoodResult = resolver.resolve(sword, AttributeEngine.KEY, woodTarget).orElseThrow();
		assertEquals(1f, swordWoodResult.getValue(MINING_SPEED), "Base (1) only.");
	}

	/**
	 * Tests a condition that depends on the state of a sibling component.
	 */
	@Test
	void appliesComplexStructuralConditions() {
		// Condition: bonus is active if the slot of type HANDLE_SLOT_TYPE contains a component with the "wood" tag.
		var headBonusCondition = new Condition(List.of(StaticConditions.slotContains(HANDLE_SLOT_TYPE, "wood")), Collections.emptyList());
		var headBonus = attribute(ATTACK_DAMAGE).withValue(5).withCondition(headBonusCondition).build();

		var head = part(PICKAXE_HEAD_ID).withTag(METAL_TAG).withProperty(headBonus).build();
		var oakHandle = part(HANDLE_ID).withTag(WOOD_TAG).build();
		var ironHandle = part(HANDLE_ID).withTag(METAL_TAG).build();

		var woodPickaxe = tool(PICKAXE_ID).withAttribute(ATTACK_DAMAGE, 1f)
				.withPart(head, "head_slot", HEAD_SLOT_TYPE)
				.withPart(oakHandle, "handle_slot", HANDLE_SLOT_TYPE)
				.build();

		var ironPickaxe = tool(PICKAXE_ID).withAttribute(ATTACK_DAMAGE, 1f)
				.withPart(head, "head_slot", HEAD_SLOT_TYPE)
				.withPart(ironHandle, "handle_slot", HANDLE_SLOT_TYPE)
				.build();

		float woodDamage = resolver.resolve(woodPickaxe, AttributeEngine.KEY).map(res -> res.getValue(ATTACK_DAMAGE)).orElse(0f);
		assertEquals(6f, woodDamage, "Base damage (1) + head bonus (5, because handle is wood) = 6");

		float ironDamage = resolver.resolve(ironPickaxe, AttributeEngine.KEY).map(res -> res.getValue(ATTACK_DAMAGE)).orElse(0f);
		assertEquals(1f, ironDamage, "Base damage (1) only, because handle is not wood.");
	}
}
