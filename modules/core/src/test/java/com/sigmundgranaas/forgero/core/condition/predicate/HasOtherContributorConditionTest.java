package com.sigmundgranaas.forgero.core.condition.predicate;

import com.sigmundgranaas.forgero.core.ForgeroTest;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.property.context.ResolutionContext;
import org.junit.jupiter.api.Test;

import static com.sigmundgranaas.forgero.testutils.ForgeroTestFactory.*;
import static com.sigmundgranaas.forgero.testutils.TestIdentifiers.*;
import static org.junit.jupiter.api.Assertions.*;

class HasOtherContributorConditionTest extends ForgeroTest {

	@Test
	void conditionPassesWhenSiblingHasSameAttributeType() {
		// Create two materials, both with durability attribute
		Component iron = part(IRON_ID)
				.withTag(METAL_TAG)
				.withAttribute(DURABILITY_IDENTIFIER, 250f)
				.build();

		Component oak = part(OAK_ID)
				.withTag(WOOD_TAG)
				.withAttribute(DURABILITY_IDENTIFIER, 60f)
				.build();

		// Create a part that contains both materials in structure slots
		Component pickaxeHead = part(PICKAXE_HEAD_ID)
				.withStructureSlot(structureSlot("material_slot", TOOL_MATERIAL_ID, iron))
				.withStructureSlot(structureSlot("handle_slot", HANDLE_SLOT_TYPE, oak))
				.build();

		// Test from iron's perspective - oak should be found as another contributor
		HasOtherContributorCondition condition = new HasOtherContributorCondition(
				id("forgero:has_other_contributor"),
				DURABILITY_IDENTIFIER
		);

		ResolutionContext ironContext = new ResolutionContext(iron, pickaxeHead);
		assertTrue(condition.test(ironContext),
				"Condition should pass when sibling has same attribute type");
	}

	@Test
	void conditionFailsWhenNoOtherContributorExists() {
		// Create only one material with durability
		Component iron = part(IRON_ID)
				.withTag(METAL_TAG)
				.withAttribute(DURABILITY_IDENTIFIER, 250f)
				.build();

		// Create another material WITHOUT durability attribute
		Component oak = part(OAK_ID)
				.withTag(WOOD_TAG)
				.withAttribute(ATTACK_DAMAGE_IDENTIFIER, 5f) // Different attribute type
				.build();

		Component pickaxeHead = part(PICKAXE_HEAD_ID)
				.withStructureSlot(structureSlot("material_slot", TOOL_MATERIAL_ID, iron))
				.withStructureSlot(structureSlot("handle_slot", HANDLE_SLOT_TYPE, oak))
				.build();

		HasOtherContributorCondition condition = new HasOtherContributorCondition(
				id("forgero:has_other_contributor"),
				DURABILITY_IDENTIFIER
		);

		ResolutionContext ironContext = new ResolutionContext(iron, pickaxeHead);
		assertFalse(condition.test(ironContext),
				"Condition should fail when no other component has same attribute type");
	}

	@Test
	void conditionPassesWhenNestedComponentHasSameAttribute() {
		// Create a deeply nested structure
		Component iron = part(IRON_ID)
				.withTag(METAL_TAG)
				.withAttribute(DURABILITY_IDENTIFIER, 250f)
				.build();

		// Create a handle with its own material that has durability
		Component handleMaterial = part(OAK_ID)
				.withTag(WOOD_TAG)
				.withAttribute(DURABILITY_IDENTIFIER, 60f)
				.build();

		Component handle = part(HANDLE_ID)
				.withTag(HANDLE_SLOT_TYPE) // Required tag for slot validation
				.withStructureSlot(structureSlot("handle_material", TOOL_MATERIAL_ID, handleMaterial))
				.build();

		// Create a pickaxe head with iron
		Component pickaxeHead = part(PICKAXE_HEAD_ID)
				.withTag(HEAD_SLOT_TYPE) // Required tag for slot validation
				.withStructureSlot(structureSlot("material_slot", TOOL_MATERIAL_ID, iron))
				.build();

		// Create the full tool with both parts (using withPart for ToolBuilder)
		Component pickaxe = tool(PICKAXE_ID)
				.withPart(pickaxeHead, "head_slot", HEAD_SLOT_TYPE)
				.withPart(handle, "handle_slot", HANDLE_SLOT_TYPE)
				.build();

		HasOtherContributorCondition condition = new HasOtherContributorCondition(
				id("forgero:has_other_contributor"),
				DURABILITY_IDENTIFIER
		);

		// Test from iron's perspective - should find handleMaterial as another contributor
		ResolutionContext ironContext = new ResolutionContext(iron, pickaxe);
		assertTrue(condition.test(ironContext),
				"Condition should pass when nested component has same attribute type");
	}

	@Test
	void conditionIgnoresSelfAttributes() {
		// Create a single material with durability - no other contributors
		Component iron = part(IRON_ID)
				.withTag(METAL_TAG)
				.withAttribute(DURABILITY_IDENTIFIER, 250f)
				.build();

		Component pickaxeHead = part(PICKAXE_HEAD_ID)
				.withStructureSlot(structureSlot("material_slot", TOOL_MATERIAL_ID, iron))
				.build();

		HasOtherContributorCondition condition = new HasOtherContributorCondition(
				id("forgero:has_other_contributor"),
				DURABILITY_IDENTIFIER
		);

		// Test from iron's perspective - should NOT count iron's own attribute
		ResolutionContext ironContext = new ResolutionContext(iron, pickaxeHead);
		assertFalse(condition.test(ironContext),
				"Condition should ignore self's own attributes when checking for other contributors");
	}

	@Test
	void conditionPassesWhenParentHasSameAttribute() {
		// Material has durability
		Component iron = part(IRON_ID)
				.withTag(METAL_TAG)
				.withAttribute(DURABILITY_IDENTIFIER, 250f)
				.build();

		// The parent part also has durability (e.g., shape modifier)
		Component pickaxeHead = part(PICKAXE_HEAD_ID)
				.withAttribute(DURABILITY_IDENTIFIER, 50f) // Parent has same attribute
				.withStructureSlot(structureSlot("material_slot", TOOL_MATERIAL_ID, iron))
				.build();

		HasOtherContributorCondition condition = new HasOtherContributorCondition(
				id("forgero:has_other_contributor"),
				DURABILITY_IDENTIFIER
		);

		// Test from iron's perspective - parent (pickaxeHead) should be found
		ResolutionContext ironContext = new ResolutionContext(iron, pickaxeHead);
		assertTrue(condition.test(ironContext),
				"Condition should pass when parent component has same attribute type");
	}

	@Test
	void conditionFailsWhenComponentIsAlone() {
		// Single component with no parent or siblings
		Component iron = part(IRON_ID)
				.withTag(METAL_TAG)
				.withAttribute(DURABILITY_IDENTIFIER, 250f)
				.build();

		HasOtherContributorCondition condition = new HasOtherContributorCondition(
				id("forgero:has_other_contributor"),
				DURABILITY_IDENTIFIER
		);

		// Iron is both self and root - no other contributors possible
		ResolutionContext ironContext = new ResolutionContext(iron, iron);
		assertFalse(condition.test(ironContext),
				"Condition should fail when component is alone (no other components in tree)");
	}
}
