package com.sigmundgranaas.forgero.core.attribute.impl;

import com.sigmundgranaas.forgero.core.ForgeroTest;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.attribute.api.Attribute;
import com.sigmundgranaas.forgero.core.attribute.api.AttributeQueryResult;
import com.sigmundgranaas.forgero.core.attribute.api.DefaultAttributes;
import com.sigmundgranaas.forgero.core.attribute.api.SimpleAttribute;
import com.sigmundgranaas.forgero.core.attribute.api.operator.MultiplicationOperator;
import com.sigmundgranaas.forgero.core.component.api.slot.SlotValidator;
import com.sigmundgranaas.forgero.core.component.api.structure.ComponentStructure;
import com.sigmundgranaas.forgero.core.component.api.structure.StructureSlot;
import com.sigmundgranaas.forgero.core.component.impl.StaticComponent;
import com.sigmundgranaas.forgero.core.component.impl.StructuredPart;
import com.sigmundgranaas.forgero.core.condition.api.Condition;
import com.sigmundgranaas.forgero.core.condition.api.StaticCondition;
import com.sigmundgranaas.forgero.core.condition.predicate.InSlotTypeCondition;
import com.sigmundgranaas.forgero.core.property.api.Resolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static com.sigmundgranaas.forgero.testutils.ForgeroTestFactory.attributeEngine;
import static com.sigmundgranaas.forgero.testutils.ForgeroTestFactory.resolver;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * PRIMARY ACCEPTANCE TEST for the slot-scoped attribute composition system.
 *
 * This test validates the core behavior:
 * - Material attributes with when_in filter only apply in the correct slot
 * - Part template modifiers multiply the material's base value
 * - Composition produces correct final values
 */
class SlotScopedCompositionTest extends ForgeroTest {

	private Resolver resolver;

	@BeforeEach
	void setUp() {
		resolver = resolver();
	}

	private StaticComponent materialWithWhenIn(OpenIdentifier id, OpenIdentifier tag,
												List<Attribute> attributes) {
		Map<String, List<?>> props = new HashMap<>();
		if (!attributes.isEmpty()) {
			props.put(Attribute.KEY.key(), attributes);
		}
		return new StaticComponent(id, Set.of(tag), props);
	}

	private SimpleAttribute attributeWithWhenIn(OpenIdentifier type, float value,
												 OpenIdentifier slotType) {
		StaticCondition whenInCondition = new InSlotTypeCondition(
				idFactory.of("forgero:in_slot_type"),
				slotType
		);
		Condition condition = new Condition(List.of(whenInCondition), Collections.emptyList());
		return new SimpleAttribute(Optional.empty(), type, value,
				com.sigmundgranaas.forgero.core.attribute.api.operator.AdditionOperator.getInstance(),
				0, condition);
	}

	/**
	 * Test: Material + Shape = Part composition
	 *
	 * Given:
	 *   - Iron material with attack_damage = 6 (when_in: material)
	 *   - Pickaxe head template with attack_damage = multiply 1.5
	 *
	 * When composed:
	 *   - Iron is placed in material slot of pickaxe_head
	 *
	 * Then:
	 *   - Final attack_damage = 6 * 1.5 = 9
	 */
	@Test
	void materialBaseTimesShapeModifierComposition() {
		// Material: Iron with attack_damage = 6 (only when in material slot)
		SimpleAttribute ironDamage = attributeWithWhenIn(
				DefaultAttributes.ATTACK_DAMAGE,
				6f,
				TOOL_MATERIAL_ID
		);
		StaticComponent iron = materialWithWhenIn(IRON_ID, METAL_TAG, List.of(ironDamage));

		// Part template: Pickaxe head with attack_damage multiply 1.5
		SimpleAttribute headModifier = new SimpleAttribute(
				Optional.empty(),
				DefaultAttributes.ATTACK_DAMAGE,
				1.5f,
				MultiplicationOperator.getInstance(),
				1,  // Higher group = applied after base
				null
		);
		Map<String, List<?>> headProps = new HashMap<>();
		headProps.put(Attribute.KEY.key(), List.of(headModifier));

		StructuredPart pickaxeHead = new StructuredPart(
				PICKAXE_HEAD_ID,
				Set.of(PICKAXE_HEAD_TAG),
				headProps,
				ComponentStructure.of(
						new StructureSlot(idFactory.of("material_slot"), TOOL_MATERIAL_ID, "", SlotValidator.ACCEPT_ALL, iron)
				)
		);

		AttributeQueryResult result = resolver.resolve(pickaxeHead, attributeEngine());

		assertEquals(9f, result.getValue(DefaultAttributes.ATTACK_DAMAGE), 0.001f,
				"Attack damage should be material base (6) * shape modifier (1.5) = 9");
	}

	/**
	 * Test: Attributes without when_in filter always apply
	 */
	@Test
	void attributesWithoutWhenInAlwaysApply() {
		// Simple attribute with no when_in filter
		SimpleAttribute unconditionalDamage = new SimpleAttribute(DefaultAttributes.ATTACK_DAMAGE, 10f);

		StaticComponent iron = materialWithWhenIn(IRON_ID, METAL_TAG, List.of(unconditionalDamage));

		StructuredPart pickaxeHead = new StructuredPart(
				PICKAXE_HEAD_ID,
				Set.of(PICKAXE_HEAD_TAG),
				new HashMap<>(),
				ComponentStructure.of(
						new StructureSlot(idFactory.of("material_slot"), TOOL_MATERIAL_ID, "", SlotValidator.ACCEPT_ALL, iron)
				)
		);

		AttributeQueryResult result = resolver.resolve(pickaxeHead, attributeEngine());

		assertEquals(10f, result.getValue(DefaultAttributes.ATTACK_DAMAGE), 0.001f,
				"Attribute without when_in should always apply");
	}

	/**
	 * Test: when_in filter prevents application in wrong slot
	 */
	@Test
	void whenInFilterPreventsApplicationInWrongSlot() {
		// Attribute that only applies when in upgrade slot
		SimpleAttribute upgradeDamage = attributeWithWhenIn(
				DefaultAttributes.ATTACK_DAMAGE,
				100f,
				idFactory.of("forgero:upgrade")  // when_in: upgrade
		);
		StaticComponent iron = materialWithWhenIn(IRON_ID, METAL_TAG, List.of(upgradeDamage));

		// But iron is placed in material slot (not upgrade slot)
		StructuredPart pickaxeHead = new StructuredPart(
				PICKAXE_HEAD_ID,
				Set.of(PICKAXE_HEAD_TAG),
				new HashMap<>(),
				ComponentStructure.of(
						new StructureSlot(idFactory.of("material_slot"), TOOL_MATERIAL_ID, "", SlotValidator.ACCEPT_ALL, iron)  // material slot, not upgrade
				)
		);

		AttributeQueryResult result = resolver.resolve(pickaxeHead, attributeEngine());

		assertEquals(0f, result.getValue(DefaultAttributes.ATTACK_DAMAGE), 0.001f,
				"Attribute should not apply when component is in wrong slot type");
	}

	/**
	 * Test: Multiple materials contribute their base values
	 */
	@Test
	void multipleSlotsMergeTheirAttributes() {
		// Head material: attack_damage = 6
		SimpleAttribute headMaterialDamage = attributeWithWhenIn(
				DefaultAttributes.ATTACK_DAMAGE, 6f, TOOL_MATERIAL_ID
		);
		StaticComponent headIron = materialWithWhenIn(IRON_ID, METAL_TAG, List.of(headMaterialDamage));

		// Binding material: attack_damage = 2
		SimpleAttribute bindingMaterialDamage = attributeWithWhenIn(
				DefaultAttributes.ATTACK_DAMAGE, 2f, TOOL_MATERIAL_ID
		);
		StaticComponent bindingIron = materialWithWhenIn(
				idFactory.of("binding_iron"), METAL_TAG, List.of(bindingMaterialDamage)
		);

		// Pickaxe head with material slot
		StructuredPart pickaxeHead = new StructuredPart(
				PICKAXE_HEAD_ID,
				Set.of(PICKAXE_HEAD_TAG),
				new HashMap<>(),
				ComponentStructure.of(
						new StructureSlot(idFactory.of("head_material"), TOOL_MATERIAL_ID, "", SlotValidator.ACCEPT_ALL, headIron)
				)
		);

		// Pickaxe with head and binding slots
		StructuredPart pickaxe = new StructuredPart(
				PICKAXE_ID,
				Set.of(idFactory.of("pickaxe")),
				new HashMap<>(),
				ComponentStructure.of(
						new StructureSlot(HEAD_SLOT_ID, PICKAXE_HEAD_TAG, "", SlotValidator.ACCEPT_ALL, pickaxeHead),
						new StructureSlot(BINDING_SLOT_ID, TOOL_MATERIAL_ID, "", SlotValidator.ACCEPT_ALL, bindingIron)
				)
		);

		AttributeQueryResult result = resolver.resolve(pickaxe, attributeEngine());

		// Both materials contribute: 6 + 2 = 8
		assertEquals(8f, result.getValue(DefaultAttributes.ATTACK_DAMAGE), 0.001f,
				"Multiple slot contents should merge their attributes");
	}
}
