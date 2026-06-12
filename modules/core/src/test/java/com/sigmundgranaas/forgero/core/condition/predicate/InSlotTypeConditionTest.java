package com.sigmundgranaas.forgero.core.condition.predicate;

import com.sigmundgranaas.forgero.core.ForgeroTest;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.slot.ComponentUpgradeSlot;
import com.sigmundgranaas.forgero.core.property.compilation.ResolutionContext;
import org.junit.jupiter.api.Test;

import static com.sigmundgranaas.forgero.testutils.ForgeroTestFactory.*;
import static com.sigmundgranaas.forgero.testutils.TestIdentifiers.*;
import static org.junit.jupiter.api.Assertions.*;

class InSlotTypeConditionTest extends ForgeroTest {

	@Test
	void conditionPassesWhenComponentIsInMatchingSlot() {
		Component iron = material(IRON_ID, METAL_TAG);
		Component pickaxeHead = part(PICKAXE_HEAD_ID)
				.withStructureSlot(structureSlot("material_slot", TOOL_MATERIAL_ID, iron))
				.build();

		InSlotTypeCondition condition = new InSlotTypeCondition(
				id("forgero:in_slot_type"),
				TOOL_MATERIAL_ID  // when_in: material
		);

		ResolutionContext context = new ResolutionContext(iron, pickaxeHead);

		assertTrue(condition.test(context), "Condition should pass when component is in matching slot type");
	}

	@Test
	void conditionFailsWhenComponentIsInDifferentSlot() {
		Component iron = material(IRON_ID, METAL_TAG);
		Component pickaxeHead = part(PICKAXE_HEAD_ID)
				.withStructureSlot(structureSlot("material_slot", TOOL_MATERIAL_ID, iron))
				.build();

		InSlotTypeCondition condition = new InSlotTypeCondition(
				id("forgero:in_slot_type"),
				id("forgero:upgrade")  // when_in: upgrade (wrong slot type)
		);

		ResolutionContext context = new ResolutionContext(iron, pickaxeHead);

		assertFalse(condition.test(context), "Condition should fail when component is in different slot type");
	}

	@Test
	void conditionFailsWhenComponentIsRoot() {
		Component pickaxeHead = part(PICKAXE_HEAD_ID).build();

		InSlotTypeCondition condition = new InSlotTypeCondition(
				id("forgero:in_slot_type"),
				TOOL_MATERIAL_ID
		);

		ResolutionContext context = new ResolutionContext(pickaxeHead, pickaxeHead);

		assertFalse(condition.test(context), "Condition should fail when component is root (not in any slot)");
	}

	/**
	 * Slot/part types reach the condition with their full authored path in a pristine component
	 * but canonicalized (last segment) once an item has been serialized to NBT and read back.
	 * Matching must be canonical so the same condition holds in both representations.
	 */
	@Test
	void conditionMatchesAcrossCanonicalAndFullPathForms() {
		OpenIdentifier fullPath = OpenIdentifier.parse("forgero:contexts/offensive");
		OpenIdentifier canonical = OpenIdentifier.parse("forgero:offensive"); // serialized form

		Component iron = material(IRON_ID, METAL_TAG);
		Component head = part(PICKAXE_HEAD_ID)
				.withStructureSlot(structureSlot("ctx_slot", fullPath, iron))
				.build();
		ResolutionContext context = new ResolutionContext(iron, head);

		assertTrue(new InSlotTypeCondition(id("forgero:in_slot_type"), canonical).test(context),
				"Canonical condition form should match a full-path slot type");
		assertTrue(new InSlotTypeCondition(id("forgero:in_slot_type"), fullPath).test(context),
				"Full-path condition form should match the same slot type");
	}

	/**
	 * An upgrade slot answers to two identities: its type (what it accepts, e.g. "in any upgrade
	 * slot") and its scope (its context, e.g. "in an offensive slot"). A single in_slot_type
	 * condition can match either.
	 */
	@Test
	void conditionMatchesUpgradeSlotByTypeOrScope() {
		OpenIdentifier slotType = OpenIdentifier.parse("forgero:materials/roles/upgrade_material");
		OpenIdentifier context = OpenIdentifier.parse("forgero:contexts/offensive");

		Component gem = material(IRON_ID, METAL_TAG);
		ComponentUpgradeSlot slot = ComponentUpgradeSlot
				.emptyWithTagsAndValidator(id("reinforcement"), slotType, "", java.util.Set.of(context),
						com.sigmundgranaas.forgero.core.component.api.slot.SlotValidator.ACCEPT_ALL)
				.withContent(gem);
		Component head = part(PICKAXE_HEAD_ID).withUpgradeSlot(slot).build();
		ResolutionContext context0 = new ResolutionContext(gem, head);

		assertTrue(new InSlotTypeCondition(id("forgero:in_slot_type"), slotType).test(context0),
				"Should match the slot's type (the 'in any upgrade slot' case)");
		assertTrue(new InSlotTypeCondition(id("forgero:in_slot_type"), context).test(context0),
				"Should match the slot's scope (the 'in an offensive slot' case)");
		assertFalse(new InSlotTypeCondition(id("forgero:in_slot_type"), OpenIdentifier.parse("forgero:contexts/defensive")).test(context0),
				"Should not match a different context");
	}
}
