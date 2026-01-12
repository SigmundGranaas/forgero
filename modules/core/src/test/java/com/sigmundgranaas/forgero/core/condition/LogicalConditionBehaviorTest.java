package com.sigmundgranaas.forgero.core.condition;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.ForgeroTest;
import com.sigmundgranaas.forgero.core.attribute.api.Attribute;
import com.sigmundgranaas.forgero.core.attribute.api.AttributeQueryResult;
import com.sigmundgranaas.forgero.core.attribute.api.DefaultAttributes;
import com.sigmundgranaas.forgero.core.attribute.api.SimpleAttribute;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.ComponentBuilder;
import com.sigmundgranaas.forgero.core.component.api.structure.ComponentPart;
import com.sigmundgranaas.forgero.core.condition.api.Condition;
import com.sigmundgranaas.forgero.core.condition.api.StaticCondition;
import com.sigmundgranaas.forgero.core.condition.predicate.InSlotTypeCondition;
import com.sigmundgranaas.forgero.core.condition.predicate.TagMatchCondition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.sigmundgranaas.forgero.testutils.ForgeroTestFactory.attributeEngine;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Logical Condition Behavior")
class LogicalConditionBehaviorTest extends ForgeroTest {

	private SimpleAttribute attributeWithCondition(OpenIdentifier type, float value, Condition condition) {
		return new SimpleAttribute(
				Optional.empty(),
				type,
				value,
				com.sigmundgranaas.forgero.core.attribute.api.operator.AdditionOperator.getInstance(),
				0,
				Optional.empty(),
				Optional.of(condition)
		);
	}

	private StaticCondition inSlotType(String slotType) {
		return new InSlotTypeCondition(
				idFactory.of("forgero:in_slot_type"),
				idFactory.of(slotType)
		);
	}

	@Nested
	@DisplayName("Single Condition Evaluation")
	class SingleConditionEvaluation {

		@Test
		@DisplayName("attribute applies when single condition passes")
		void attributeAppliesWhenSingleConditionPasses() {
			Condition condition = new Condition(List.of(inSlotType("tool_material")), Collections.emptyList());
			SimpleAttribute damage = attributeWithCondition(DefaultAttributes.ATTACK_DAMAGE, 5f, condition);

			Component iron = new ComponentBuilder("forgero:iron")
					.tag("forgero:metal")
					.tag("forgero:tool_material")
					.property(Attribute.KEY.key(), damage)
					.build();

			Component tool = new ComponentBuilder("forgero:pickaxe")
					.structureSlot(
							OpenIdentifier.of("material_slot"),
							OpenIdentifier.of("tool_material"),
							"Material slot",
							iron
					)
					.build();

			AttributeQueryResult result = attributeEngine().resolve(tool);

			assertEquals(5f, result.getValue(DefaultAttributes.ATTACK_DAMAGE), 0.001f);
		}

		@Test
		@DisplayName("attribute does not apply when single condition fails")
		void attributeDoesNotApplyWhenSingleConditionFails() {
			Condition condition = new Condition(List.of(inSlotType("armor_material")), Collections.emptyList());
			SimpleAttribute damage = attributeWithCondition(DefaultAttributes.ATTACK_DAMAGE, 5f, condition);

			Component iron = new ComponentBuilder("forgero:iron")
					.tag("forgero:tool_material")
					.property(Attribute.KEY.key(), damage)
					.build();

			Component tool = new ComponentBuilder("forgero:pickaxe")
					.structureSlot(
							OpenIdentifier.of("material_slot"),
							OpenIdentifier.of("tool_material"),
							"Material slot",
							iron
					)
					.build();

			AttributeQueryResult result = attributeEngine().resolve(tool);

			assertEquals(0f, result.getValue(DefaultAttributes.ATTACK_DAMAGE), 0.001f);
		}
	}

	@Nested
	@DisplayName("AND Condition Evaluation")
	class AndConditionEvaluation {

		@Test
		@DisplayName("attribute applies when all AND conditions pass")
		void attributeAppliesWhenAllAndConditionsPass() {
			StaticCondition slotCondition = inSlotType("tool_material");
			StaticCondition tagCondition = new com.sigmundgranaas.forgero.core.condition.predicate.HasOtherContributorCondition(
					idFactory.of("forgero:has_other_contributor"),
					DefaultAttributes.DURABILITY
			);

			Condition andCondition = new Condition(List.of(slotCondition), Collections.emptyList());
			SimpleAttribute damage = attributeWithCondition(DefaultAttributes.ATTACK_DAMAGE, 5f, andCondition);
			SimpleAttribute durability = new SimpleAttribute(DefaultAttributes.DURABILITY, 100f);

			Component iron = new ComponentBuilder("forgero:iron")
					.tag("forgero:metal")
					.tag("forgero:tool_material")
					.property(Attribute.KEY.key(), damage)
					.build();

			Component shape = new ComponentBuilder("forgero:pickaxe_shape")
					.tag("forgero:shape")
					.property(Attribute.KEY.key(), durability)
					.build();

			Component part = new ComponentBuilder("forgero:pickaxe_head")
					.structureSlot(OpenIdentifier.of("material"), OpenIdentifier.of("tool_material"), "Material", iron)
					.structureSlot(OpenIdentifier.of("shape"), OpenIdentifier.of("shape"), "Shape", shape)
					.build();

			AttributeQueryResult result = attributeEngine().resolve(part);

			assertEquals(5f, result.getValue(DefaultAttributes.ATTACK_DAMAGE), 0.001f);
		}

		@Test
		@DisplayName("attribute does not apply when any AND condition fails")
		void attributeDoesNotApplyWhenAnyAndConditionFails() {
			StaticCondition slotCondition = inSlotType("armor_material");

			Condition andCondition = new Condition(List.of(slotCondition), Collections.emptyList());
			SimpleAttribute damage = attributeWithCondition(DefaultAttributes.ATTACK_DAMAGE, 10f, andCondition);

			Component iron = new ComponentBuilder("forgero:iron")
					.tag("forgero:metal")
					.tag("forgero:tool_material")
					.property(Attribute.KEY.key(), damage)
					.build();

			Component tool = new ComponentBuilder("forgero:pickaxe")
					.structureSlot(OpenIdentifier.of("material"), OpenIdentifier.of("tool_material"), "Material", iron)
					.build();

			AttributeQueryResult result = attributeEngine().resolve(tool);

			assertEquals(0f, result.getValue(DefaultAttributes.ATTACK_DAMAGE), 0.001f);
		}

		@Test
		@DisplayName("multiple static conditions all evaluated")
		void multipleStaticConditionsAllEvaluated() {
			StaticCondition slotCondition1 = inSlotType("tool_material");
			StaticCondition slotCondition2 = inSlotType("tool_material");

			Condition andCondition = new Condition(List.of(slotCondition1, slotCondition2), Collections.emptyList());
			SimpleAttribute damage = attributeWithCondition(DefaultAttributes.ATTACK_DAMAGE, 7f, andCondition);

			Component iron = new ComponentBuilder("forgero:iron")
					.tag("forgero:tool_material")
					.property(Attribute.KEY.key(), damage)
					.build();

			Component tool = new ComponentBuilder("forgero:pickaxe")
					.structureSlot(OpenIdentifier.of("material"), OpenIdentifier.of("tool_material"), "Material", iron)
					.build();

			AttributeQueryResult result = attributeEngine().resolve(tool);

			assertEquals(7f, result.getValue(DefaultAttributes.ATTACK_DAMAGE), 0.001f);
		}
	}

	@Nested
	@DisplayName("Condition with No Conditions (Always True)")
	class AlwaysTrueCondition {

		@Test
		@DisplayName("empty condition list means always true")
		void emptyConditionListMeansAlwaysTrue() {
			Condition alwaysTrue = Condition.ALWAYS_TRUE;
			SimpleAttribute damage = attributeWithCondition(DefaultAttributes.ATTACK_DAMAGE, 3f, alwaysTrue);

			Component iron = new ComponentBuilder("forgero:iron")
					.property(Attribute.KEY.key(), damage)
					.build();

			AttributeQueryResult result = attributeEngine().resolve(iron);

			assertEquals(3f, result.getValue(DefaultAttributes.ATTACK_DAMAGE), 0.001f);
		}

		@Test
		@DisplayName("unconditional attribute always applies")
		void unconditionalAttributeAlwaysApplies() {
			SimpleAttribute damage = new SimpleAttribute(DefaultAttributes.ATTACK_DAMAGE, 4f);

			Component iron = new ComponentBuilder("forgero:iron")
					.property(Attribute.KEY.key(), damage)
					.build();

			AttributeQueryResult result = attributeEngine().resolve(iron);

			assertEquals(4f, result.getValue(DefaultAttributes.ATTACK_DAMAGE), 0.001f);
		}
	}

	@Nested
	@DisplayName("Nested Component Condition Propagation")
	class NestedComponentConditionPropagation {

		@Test
		@DisplayName("conditions evaluate correctly in nested structure")
		void conditionsEvaluateCorrectlyInNestedStructure() {
			Condition toolMaterialCondition = new Condition(List.of(inSlotType("tool_material")), Collections.emptyList());
			SimpleAttribute ironDamage = attributeWithCondition(DefaultAttributes.ATTACK_DAMAGE, 6f, toolMaterialCondition);

			Component iron = new ComponentBuilder("forgero:iron")
					.tag("forgero:tool_material")
					.property(Attribute.KEY.key(), ironDamage)
					.build();

			Component pickaxeHead = new ComponentBuilder("forgero:pickaxe_head")
					.tag("forgero:pickaxe_head")
					.structureSlot(OpenIdentifier.of("material"), OpenIdentifier.of("tool_material"), "Material", iron)
					.build();

			Component handle = new ComponentBuilder("forgero:oak_handle")
					.tag("forgero:handle")
					.build();

			Component pickaxe = new ComponentBuilder("forgero:pickaxe")
					.structureSlot(OpenIdentifier.of("head"), OpenIdentifier.of("pickaxe_head"), "Head", pickaxeHead)
					.structureSlot(OpenIdentifier.of("handle"), OpenIdentifier.of("handle"), "Handle", handle)
					.asEquipment()
					.build();

			AttributeQueryResult result = attributeEngine().resolve(pickaxe);

			assertEquals(6f, result.getValue(DefaultAttributes.ATTACK_DAMAGE), 0.001f);
		}

		@Test
		@DisplayName("multiple materials with conditions combine correctly")
		void multipleMaterialsWithConditionsCombineCorrectly() {
			Condition toolMaterialCondition = new Condition(List.of(inSlotType("tool_material")), Collections.emptyList());

			SimpleAttribute ironDamage = attributeWithCondition(DefaultAttributes.ATTACK_DAMAGE, 4f, toolMaterialCondition);
			SimpleAttribute oakDamage = attributeWithCondition(DefaultAttributes.ATTACK_DAMAGE, 2f, toolMaterialCondition);

			Component iron = new ComponentBuilder("forgero:iron")
					.tag("forgero:tool_material")
					.property(Attribute.KEY.key(), ironDamage)
					.build();

			Component oak = new ComponentBuilder("forgero:oak")
					.tag("forgero:tool_material")
					.property(Attribute.KEY.key(), oakDamage)
					.build();

			Component tool = new ComponentBuilder("forgero:pickaxe")
					.structureSlot(OpenIdentifier.of("head_material"), OpenIdentifier.of("tool_material"), "Head", iron)
					.structureSlot(OpenIdentifier.of("handle_material"), OpenIdentifier.of("tool_material"), "Handle", oak)
					.build();

			AttributeQueryResult result = attributeEngine().resolve(tool);

			assertEquals(6f, result.getValue(DefaultAttributes.ATTACK_DAMAGE), 0.001f);
		}
	}

	@Nested
	@DisplayName("Condition Prevents Attribute Leaking")
	class ConditionPreventsAttributeLeaking {

		@Test
		@DisplayName("armor attribute does not leak to tool context")
		void armorAttributeDoesNotLeakToToolContext() {
			Condition armorCondition = new Condition(List.of(inSlotType("armor_material")), Collections.emptyList());
			SimpleAttribute armorValue = attributeWithCondition(DefaultAttributes.ARMOR, 5f, armorCondition);

			Component iron = new ComponentBuilder("forgero:iron")
					.tag("forgero:tool_material")
					.property(Attribute.KEY.key(), armorValue)
					.build();

			Component tool = new ComponentBuilder("forgero:pickaxe")
					.structureSlot(OpenIdentifier.of("material"), OpenIdentifier.of("tool_material"), "Material", iron)
					.build();

			AttributeQueryResult result = attributeEngine().resolve(tool);

			assertEquals(0f, result.getValue(DefaultAttributes.ARMOR), 0.001f);
		}

		@Test
		@DisplayName("tool attribute does not leak to armor context")
		void toolAttributeDoesNotLeakToArmorContext() {
			Condition toolCondition = new Condition(List.of(inSlotType("tool_material")), Collections.emptyList());
			SimpleAttribute miningSpeed = attributeWithCondition(DefaultAttributes.MINING_SPEED, 8f, toolCondition);

			Component iron = new ComponentBuilder("forgero:iron")
					.tag("forgero:armor_material")
					.property(Attribute.KEY.key(), miningSpeed)
					.build();

			Component armor = new ComponentBuilder("forgero:chestplate")
					.structureSlot(OpenIdentifier.of("material"), OpenIdentifier.of("armor_material"), "Material", iron)
					.build();

			AttributeQueryResult result = attributeEngine().resolve(armor);

			assertEquals(0f, result.getValue(DefaultAttributes.MINING_SPEED), 0.001f);
		}
	}

	@Nested
	@DisplayName("Attribute Order and Operators")
	class AttributeOrderAndOperators {

		@Test
		@DisplayName("addition operators sum values")
		void additionOperatorsSumValues() {
			SimpleAttribute base = new SimpleAttribute(DefaultAttributes.DURABILITY, 100f);
			SimpleAttribute bonus = new SimpleAttribute(DefaultAttributes.DURABILITY, 50f);

			Component iron = new ComponentBuilder("forgero:iron")
					.property(Attribute.KEY.key(), base)
					.property(Attribute.KEY.key(), bonus)
					.build();

			AttributeQueryResult result = attributeEngine().resolve(iron);

			assertEquals(150f, result.getValue(DefaultAttributes.DURABILITY), 0.001f);
		}

		@Test
		@DisplayName("multiplication operator applied after addition")
		void multiplicationOperatorAppliedAfterAddition() {
			SimpleAttribute base = new SimpleAttribute(
					Optional.empty(),
					DefaultAttributes.DURABILITY,
					100f,
					com.sigmundgranaas.forgero.core.attribute.api.operator.AdditionOperator.getInstance(),
					0,
					Optional.empty(),
					Optional.empty()
			);
			SimpleAttribute multiplier = new SimpleAttribute(
					Optional.empty(),
					DefaultAttributes.DURABILITY,
					1.5f,
					com.sigmundgranaas.forgero.core.attribute.api.operator.MultiplicationOperator.getInstance(),
					1,
					Optional.empty(),
					Optional.empty()
			);

			Component iron = new ComponentBuilder("forgero:iron")
					.property(Attribute.KEY.key(), base)
					.property(Attribute.KEY.key(), multiplier)
					.build();

			AttributeQueryResult result = attributeEngine().resolve(iron);

			assertEquals(150f, result.getValue(DefaultAttributes.DURABILITY), 0.001f);
		}
	}
}
