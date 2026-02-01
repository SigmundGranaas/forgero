package com.sigmundgranaas.forgero.core.attribute.impl;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.ForgeroTest;
import com.sigmundgranaas.forgero.core.attribute.api.Attribute;
import com.sigmundgranaas.forgero.core.attribute.api.AttributeScope;
import com.sigmundgranaas.forgero.core.attribute.api.AttributeQueryResult;
import com.sigmundgranaas.forgero.core.attribute.api.DefaultAttributes;
import com.sigmundgranaas.forgero.core.attribute.api.SimpleAttribute;
import com.sigmundgranaas.forgero.core.attribute.api.operator.AdditionOperator;
import com.sigmundgranaas.forgero.core.attribute.api.operator.MultiplicationOperator;
import com.sigmundgranaas.forgero.core.component.api.slot.SlotValidator;
import com.sigmundgranaas.forgero.core.component.api.structure.ComponentPart;
import com.sigmundgranaas.forgero.core.component.api.structure.ComponentStructure;
import com.sigmundgranaas.forgero.core.component.impl.StaticComponent;
import com.sigmundgranaas.forgero.core.component.impl.StructuredEquipment;
import com.sigmundgranaas.forgero.core.component.impl.StructuredPart;
import com.sigmundgranaas.forgero.core.condition.api.Condition;
import com.sigmundgranaas.forgero.core.condition.api.StaticCondition;
import com.sigmundgranaas.forgero.core.condition.predicate.InSlotTypeCondition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static com.sigmundgranaas.forgero.testutils.ForgeroTestFactory.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the attribute composition flow for part-constructed components.
 *
 * <h2>Documented Flow</h2>
 * <ol>
 *   <li>Filter all attributes with context (part-composite)</li>
 *   <li>Apply conditions to filter attributes (e.g., in_slot_type)</li>
 *   <li>Combine and transform composite attributes (base × multiplier)</li>
 *   <li>Discard attributes with context that weren't transformed</li>
 *   <li>Pass through default (no context) attributes unchanged</li>
 * </ol>
 */
@DisplayName("Attribute Composition Flow Tests")
class PartCompositeScopeConditionTest extends ForgeroTest {

	private static final OpenIdentifier TOOL_MATERIAL_SLOT = idFactory.of("forgero:materials/roles/tool_material");
	private static final OpenIdentifier ARMOR_MATERIAL_SLOT = idFactory.of("forgero:materials/roles/armor_material");
	private static final OpenIdentifier PICKAXE_HEAD_TAG = idFactory.of("forgero:parts/types/pickaxe_head");

	private SimpleAttribute partCompositeBase(OpenIdentifier type, float value) {
		return new SimpleAttribute(
				Optional.empty(), type, value, AdditionOperator.getInstance(),
				0, Optional.of(AttributeScope.PART_COMPOSITE), Optional.empty()
		);
	}

	private SimpleAttribute partCompositeBaseWithCondition(OpenIdentifier type, float value, OpenIdentifier slotType) {
		StaticCondition condition = new InSlotTypeCondition(idFactory.of("forgero:in_slot_type"), slotType);
		return new SimpleAttribute(
				Optional.empty(), type, value, AdditionOperator.getInstance(),
				0, Optional.of(AttributeScope.PART_COMPOSITE),
				Optional.of(new Condition(List.of(condition), Collections.emptyList()))
		);
	}

	private SimpleAttribute partCompositeMultiplier(OpenIdentifier type, float value) {
		return new SimpleAttribute(
				Optional.empty(), type, value, MultiplicationOperator.getInstance(),
				0, Optional.of(AttributeScope.PART_COMPOSITE), Optional.empty()
		);
	}

	@Nested
	@DisplayName("Step 2: Condition filtering before composition")
	class ConditionFilteringTests {

		@Test
		@DisplayName("Armor attribute filtered out when in tool_material slot")
		void armorFilteredInToolMaterialSlot() {
			StaticComponent ironMaterial = new StaticComponent(
					idFactory.of("forgero:iron"),
					Set.of(idFactory.of("forgero:metal")),
					Map.of(Attribute.KEY.key(), List.of(
							partCompositeBaseWithCondition(DefaultAttributes.DURABILITY, 240f, TOOL_MATERIAL_SLOT),
							partCompositeBaseWithCondition(DefaultAttributes.ARMOR, 2f, ARMOR_MATERIAL_SLOT)
					))
			);

			StaticComponent shape = new StaticComponent(
					idFactory.of("forgero:pickaxe_head_shape"),
					Set.of(idFactory.of("forgero:shape")),
					Map.of(Attribute.KEY.key(), List.of(
							partCompositeMultiplier(DefaultAttributes.DURABILITY, 1.0f),
							partCompositeMultiplier(DefaultAttributes.ARMOR, 1.0f)
					))
			);

			StructuredPart pickaxeHead = new StructuredPart(
					idFactory.of("forgero:iron-pickaxe_head"),
					Set.of(PICKAXE_HEAD_TAG),
					new HashMap<>(),
					ComponentStructure.of(
							new ComponentPart(idFactory.of("material"), TOOL_MATERIAL_SLOT, "", SlotValidator.ACCEPT_ALL, ironMaterial),
							new ComponentPart(idFactory.of("shape"), idFactory.of("forgero:shape"), "", SlotValidator.ACCEPT_ALL, shape)
					)
			);

			AttributeQueryResult result = attributeEngine().resolve(pickaxeHead);

			assertEquals(240f, result.getValue(DefaultAttributes.DURABILITY), 0.001f,
					"Durability passes condition and composes");
			assertEquals(0f, result.getValue(DefaultAttributes.ARMOR), 0.001f,
					"Armor filtered by in_slot_type condition before composition");
		}

		@Test
		@DisplayName("Armor attribute passes when in armor_material slot")
		void armorPassesInArmorMaterialSlot() {
			StaticComponent ironMaterial = new StaticComponent(
					idFactory.of("forgero:iron"),
					Set.of(idFactory.of("forgero:metal")),
					Map.of(Attribute.KEY.key(), List.of(
							partCompositeBaseWithCondition(DefaultAttributes.ARMOR, 2f, ARMOR_MATERIAL_SLOT)
					))
			);

			StaticComponent shape = new StaticComponent(
					idFactory.of("forgero:armor_plate_shape"),
					Set.of(idFactory.of("forgero:shape")),
					Map.of(Attribute.KEY.key(), List.of(
							partCompositeMultiplier(DefaultAttributes.ARMOR, 1.0f)
					))
			);

			StructuredPart armorPlate = new StructuredPart(
					idFactory.of("forgero:iron-armor_plate"),
					Set.of(idFactory.of("forgero:armor_plate")),
					new HashMap<>(),
					ComponentStructure.of(
							new ComponentPart(idFactory.of("material"), ARMOR_MATERIAL_SLOT, "", SlotValidator.ACCEPT_ALL, ironMaterial),
							new ComponentPart(idFactory.of("shape"), idFactory.of("forgero:shape"), "", SlotValidator.ACCEPT_ALL, shape)
					)
			);

			AttributeQueryResult result = attributeEngine().resolve(armorPlate);

			assertEquals(2f, result.getValue(DefaultAttributes.ARMOR), 0.001f,
					"Armor passes condition in armor_material slot");
		}
	}

	@Nested
	@DisplayName("Step 3: Combine and transform composite attributes")
	class CompositionTests {

		@Test
		@DisplayName("Base × multiplier composition from different sources")
		void baseTimesMultiplierComposition() {
			StaticComponent material = new StaticComponent(
					idFactory.of("forgero:iron"),
					Set.of(idFactory.of("forgero:metal")),
					Map.of(Attribute.KEY.key(), List.of(
							partCompositeBase(DefaultAttributes.MINING_SPEED, 6f)
					))
			);

			StaticComponent shape = new StaticComponent(
					idFactory.of("forgero:pickaxe_head_shape"),
					Set.of(idFactory.of("forgero:shape")),
					Map.of(Attribute.KEY.key(), List.of(
							partCompositeMultiplier(DefaultAttributes.MINING_SPEED, 1.2f)
					))
			);

			StructuredPart part = new StructuredPart(
					idFactory.of("test-part"),
					Set.of(idFactory.of("forgero:test")),
					new HashMap<>(),
					ComponentStructure.of(
							new ComponentPart(idFactory.of("material"), TOOL_MATERIAL_SLOT, "", SlotValidator.ACCEPT_ALL, material),
							new ComponentPart(idFactory.of("shape"), idFactory.of("forgero:shape"), "", SlotValidator.ACCEPT_ALL, shape)
					)
			);

			AttributeQueryResult result = attributeEngine().resolve(part);

			assertEquals(7.2f, result.getValue(DefaultAttributes.MINING_SPEED), 0.001f,
					"Mining speed composed: 6 × 1.2 = 7.2");
		}
	}

	@Nested
	@DisplayName("Step 4: Discard untransformed context attributes")
	class DiscardUntransformedTests {

		@Test
		@DisplayName("Attribute with no matching multiplier is discarded")
		void attributeWithNoMultiplierDiscarded() {
			StaticComponent material = new StaticComponent(
					idFactory.of("forgero:iron"),
					Set.of(idFactory.of("forgero:metal")),
					Map.of(Attribute.KEY.key(), List.of(
							partCompositeBase(DefaultAttributes.DURABILITY, 240f),
							partCompositeBase(DefaultAttributes.ARMOR, 5f)
					))
			);

			StaticComponent shape = new StaticComponent(
					idFactory.of("forgero:pickaxe_head_shape"),
					Set.of(idFactory.of("forgero:shape")),
					Map.of(Attribute.KEY.key(), List.of(
							partCompositeMultiplier(DefaultAttributes.DURABILITY, 1.0f)
					))
			);

			StructuredPart part = new StructuredPart(
					idFactory.of("test-part"),
					Set.of(idFactory.of("forgero:test")),
					new HashMap<>(),
					ComponentStructure.of(
							new ComponentPart(idFactory.of("material"), TOOL_MATERIAL_SLOT, "", SlotValidator.ACCEPT_ALL, material),
							new ComponentPart(idFactory.of("shape"), idFactory.of("forgero:shape"), "", SlotValidator.ACCEPT_ALL, shape)
					)
			);

			AttributeQueryResult result = attributeEngine().resolve(part);

			assertEquals(240f, result.getValue(DefaultAttributes.DURABILITY), 0.001f,
					"Durability has matching multiplier");
			assertEquals(0f, result.getValue(DefaultAttributes.ARMOR), 0.001f,
					"Armor discarded - no multiplier from shape");
		}

		@Test
		@DisplayName("Single source context attributes are discarded")
		void singleSourceContextAttributesDiscarded() {
			StaticComponent material = new StaticComponent(
					idFactory.of("forgero:iron"),
					Set.of(idFactory.of("forgero:metal")),
					Map.of(Attribute.KEY.key(), List.of(
							partCompositeBase(DefaultAttributes.DURABILITY, 240f)
					))
			);

			StructuredPart part = new StructuredPart(
					idFactory.of("test-part"),
					Set.of(idFactory.of("forgero:test")),
					new HashMap<>(),
					ComponentStructure.of(
							new ComponentPart(idFactory.of("material"), TOOL_MATERIAL_SLOT, "", SlotValidator.ACCEPT_ALL, material)
					)
			);

			AttributeQueryResult result = attributeEngine().resolve(part);

			assertEquals(0f, result.getValue(DefaultAttributes.DURABILITY), 0.001f,
					"Context attribute from single source is discarded");
		}
	}

	@Nested
	@DisplayName("Step 5: Default attributes pass through")
	class DefaultAttributeTests {

		@Test
		@DisplayName("Attributes without context pass through unchanged")
		void defaultAttributesPassThrough() {
			StaticComponent material = new StaticComponent(
					idFactory.of("forgero:iron"),
					Set.of(idFactory.of("forgero:metal")),
					Map.of(Attribute.KEY.key(), List.of(
							new SimpleAttribute(DefaultAttributes.DURABILITY, 100f)
					))
			);

			StructuredPart part = new StructuredPart(
					idFactory.of("test-part"),
					Set.of(idFactory.of("forgero:test")),
					new HashMap<>(),
					ComponentStructure.of(
							new ComponentPart(idFactory.of("material"), TOOL_MATERIAL_SLOT, "", SlotValidator.ACCEPT_ALL, material)
					)
			);

			AttributeQueryResult result = attributeEngine().resolve(part);

			assertEquals(100f, result.getValue(DefaultAttributes.DURABILITY), 0.001f,
					"Default attribute passes through unchanged");
		}
	}

	@Nested
	@DisplayName("Full hierarchy: tool composition")
	class FullHierarchyTests {

		@Test
		@DisplayName("Complete pickaxe excludes armor throughout hierarchy")
		void completePickaxeExcludesArmor() {
			StaticComponent ironMaterial = new StaticComponent(
					idFactory.of("forgero:iron"),
					Set.of(idFactory.of("forgero:metal")),
					Map.of(Attribute.KEY.key(), List.of(
							partCompositeBase(DefaultAttributes.DURABILITY, 240f),
							partCompositeBase(DefaultAttributes.MINING_SPEED, 6f),
							partCompositeBase(DefaultAttributes.ARMOR, 2f)
					))
			);

			StaticComponent pickaxeHeadShape = new StaticComponent(
					idFactory.of("forgero:pickaxe_head_shape"),
					Set.of(idFactory.of("forgero:shape")),
					Map.of(Attribute.KEY.key(), List.of(
							partCompositeMultiplier(DefaultAttributes.DURABILITY, 1.0f),
							partCompositeMultiplier(DefaultAttributes.MINING_SPEED, 1.2f)
					))
			);

			StaticComponent oakMaterial = new StaticComponent(
					idFactory.of("forgero:oak"),
					Set.of(idFactory.of("forgero:wood")),
					Map.of(Attribute.KEY.key(), List.of(
							partCompositeBase(DefaultAttributes.DURABILITY, 60f)
					))
			);

			StaticComponent handleShape = new StaticComponent(
					idFactory.of("forgero:handle_shape"),
					Set.of(idFactory.of("forgero:shape")),
					Map.of(Attribute.KEY.key(), List.of(
							partCompositeMultiplier(DefaultAttributes.DURABILITY, 1.0f)
					))
			);

			StructuredPart ironPickaxeHead = new StructuredPart(
					idFactory.of("forgero:iron-pickaxe_head"),
					Set.of(PICKAXE_HEAD_TAG),
					new HashMap<>(),
					ComponentStructure.of(
							new ComponentPart(idFactory.of("material"), TOOL_MATERIAL_SLOT, "", SlotValidator.ACCEPT_ALL, ironMaterial),
							new ComponentPart(idFactory.of("shape"), idFactory.of("forgero:shape"), "", SlotValidator.ACCEPT_ALL, pickaxeHeadShape)
					)
			);

			StructuredPart oakHandle = new StructuredPart(
					idFactory.of("forgero:oak-handle"),
					Set.of(idFactory.of("forgero:handle")),
					new HashMap<>(),
					ComponentStructure.of(
							new ComponentPart(idFactory.of("material"), TOOL_MATERIAL_SLOT, "", SlotValidator.ACCEPT_ALL, oakMaterial),
							new ComponentPart(idFactory.of("shape"), idFactory.of("forgero:shape"), "", SlotValidator.ACCEPT_ALL, handleShape)
					)
			);

			StructuredEquipment ironPickaxe = StructuredEquipment.create(
					idFactory.of("forgero:iron-pickaxe"),
					Set.of(idFactory.of("forgero:tools/pickaxe")),
					new HashMap<>(),
					ComponentStructure.of(
							new ComponentPart(idFactory.of("head"), PICKAXE_HEAD_TAG, "", SlotValidator.ACCEPT_ALL, ironPickaxeHead),
							new ComponentPart(idFactory.of("handle"), idFactory.of("forgero:handle"), "", SlotValidator.ACCEPT_ALL, oakHandle)
					)
			);

			AttributeQueryResult result = attributeEngine().resolve(ironPickaxe);

			assertEquals(0f, result.getValue(DefaultAttributes.ARMOR), 0.001f,
					"Armor excluded - no multiplier at part level");
			assertEquals(300f, result.getValue(DefaultAttributes.DURABILITY), 0.001f,
					"Durability: head(240×1.0) + handle(60×1.0) = 300");
			assertEquals(7.2f, result.getValue(DefaultAttributes.MINING_SPEED), 0.001f,
					"Mining speed: head only (6×1.2) = 7.2");
		}
	}
}
