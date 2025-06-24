package com.sigmundgranaas.forgero.core.property.engine;

import static com.sigmundgranaas.forgero.core.attribute.api.DefaultAttributes.ATTACK_DAMAGE;
import static com.sigmundgranaas.forgero.core.attribute.api.DefaultAttributes.MINING_SPEED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sigmundgranaas.forgero.core.ForgeroTest;
import com.sigmundgranaas.forgero.core.attribute.api.Attribute;
import com.sigmundgranaas.forgero.core.attribute.api.AttributeQueryResult;
import com.sigmundgranaas.forgero.core.attribute.impl.AttributeEngine;
import com.sigmundgranaas.forgero.core.component.structure.ComponentStructure;
import com.sigmundgranaas.forgero.core.component.variant.StructuredPart;
import com.sigmundgranaas.forgero.core.property.api.DataTypeEngine;
import com.sigmundgranaas.forgero.core.property.api.Resolver;
import com.sigmundgranaas.forgero.core.property.condition.Condition;
import com.sigmundgranaas.forgero.core.property.condition.DynamicCondition;
import com.sigmundgranaas.forgero.core.property.condition.StaticConditions;
import com.sigmundgranaas.forgero.core.property.context.ContextKeys;
import com.sigmundgranaas.forgero.core.property.context.DynamicContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Validates the core functionality of the {@link AttributeEngine} and its interaction
 * with the new {@link Resolver} and {@link AttributeQueryResult}.
 */
class AttributeResolverTest extends ForgeroTest {
	private Resolver resolver;

	@BeforeEach
	void setUp() {
		List<DataTypeEngine<?, ?>> engines = List.of(new AttributeEngine());
		resolver = new ResolverEngine(engines);
	}

	/**
	 * Tests if the resolver can correctly resolve attributes and that the query result
	 * can provide separate values.
	 */
	@Test
	void resolvesAndCalculatesCorrectly() {
		var head = part(PICKAXE_HEAD_ID, METAL_TAG, List.of(new Attribute(ATTACK_DAMAGE, 10f)));
		var handle = part(HANDLE_ID, WOOD_TAG, List.of(new Attribute(MINING_SPEED, 5f)));
		var structure = new ComponentStructure(List.of(slot(HEAD_SLOT_ID, PICKAXE_HEAD_TAG, head), slot(HANDLE_SLOT_ID, HANDLE_TAG, handle)));
		var pickaxe = new StructuredPart(PICKAXE_ID, Set.of(idFactory.of("tool")), List.of(new Attribute(ATTACK_DAMAGE, 1f)), structure);

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
				.map(tags -> tags.contains(idFactory.of("stone")))
				.orElse(false);
		var diamondCondition = new Condition(Collections.emptyList(), List.of(onStone));
		var diamond = material(DIAMOND_ID, GEM_TAG, List.of(new Attribute(MINING_SPEED, 10f, diamondCondition)));

		// STATIC CONDITION: Active only if the root item is a 'pickaxe'.
		var iron = material(IRON_ID, METAL_TAG, List.of(new Attribute(MINING_SPEED, 5f, new Condition(List.of(StaticConditions.rootHasTag("pickaxe")), Collections.emptyList()))));

		var head = new StructuredPart(PICKAXE_HEAD_ID, Set.of(), List.of(), new ComponentStructure(List.of(slot(idFactory.of("material_slot"), MATERIAL_ID, iron), slot(GEM_SLOT_ID, GEM_SLOT_TYPE_TAG, diamond))));
		var pickaxe = new StructuredPart(PICKAXE_ID, Set.of(idFactory.of("pickaxe"), idFactory.of("tool")), List.of(new Attribute(MINING_SPEED, 1f)), new ComponentStructure(List.of(slot(HEAD_SLOT_ID, PICKAXE_HEAD_TAG, head))));
		var sword = new StructuredPart(SWORD_ID, Set.of(idFactory.of("sword"), idFactory.of("tool")), List.of(new Attribute(MINING_SPEED, 1f)), new ComponentStructure(List.of(slot(idFactory.of("blade_slot"), BLADE_TAG, head))));

		DynamicContext stoneTarget = new DynamicContext.Builder().put(ContextKeys.TARGET_TAGS, Set.of(idFactory.of("stone"))).build();
		DynamicContext woodTarget = new DynamicContext.Builder().put(ContextKeys.TARGET_TAGS, Set.of(idFactory.of("wood"))).build();

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
		// Condition: bonus is active if the slot of type HANDLE_TAG contains a component with the "wood" tag.
		var headBonus = new Attribute(ATTACK_DAMAGE, 5, new Condition(List.of(StaticConditions.slotContains(HANDLE_TAG, "wood")), Collections.emptyList()));
		var head = part(PICKAXE_HEAD_ID, METAL_TAG, List.of(headBonus));
		var oakHandle = part(HANDLE_ID, WOOD_TAG);
		var ironHandle = part(HANDLE_ID, METAL_TAG);

		var woodPickaxe = new StructuredPart(PICKAXE_ID, Set.of(), List.of(new Attribute(ATTACK_DAMAGE, 1f)), new ComponentStructure(List.of(slot(HEAD_SLOT_ID, PICKAXE_HEAD_TAG, head), slot(HANDLE_SLOT_ID, HANDLE_TAG, oakHandle))));
		var ironPickaxe = new StructuredPart(PICKAXE_ID, Set.of(), List.of(new Attribute(ATTACK_DAMAGE, 1f)), new ComponentStructure(List.of(slot(HEAD_SLOT_ID, PICKAXE_HEAD_TAG, head), slot(HANDLE_SLOT_ID, HANDLE_TAG, ironHandle))));

		float woodDamage = resolver.resolve(woodPickaxe, AttributeEngine.KEY).map(res -> res.getValue(ATTACK_DAMAGE)).orElse(0f);
		assertEquals(6f, woodDamage, "Base damage (1) + head bonus (5, because handle is wood) = 6");

		float ironDamage = resolver.resolve(ironPickaxe, AttributeEngine.KEY).map(res -> res.getValue(ATTACK_DAMAGE)).orElse(0f);
		assertEquals(1f, ironDamage, "Base damage (1) only, because handle is not wood.");
	}
}
