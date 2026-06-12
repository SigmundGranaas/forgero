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
	 * Slot/part types are path-preserving end-to-end, so a condition matches a multi-segment slot
	 * type by its exact full path — and must NOT collide distinct types that share a final segment
	 * (e.g. {@code materials/types/gem} vs {@code upgrades/types/gem}).
	 */
	@Test
	void conditionMatchesFullPathExactlyAndDoesNotCollideOnFinalSegment() {
		OpenIdentifier slotPath = OpenIdentifier.parse("forgero:materials/types/gem");
		OpenIdentifier collidingPath = OpenIdentifier.parse("forgero:upgrades/types/gem"); // same final segment
		OpenIdentifier lastSegment = OpenIdentifier.parse("forgero:gem");

		Component iron = material(IRON_ID, METAL_TAG);
		Component head = part(PICKAXE_HEAD_ID)
				.withStructureSlot(structureSlot("gem_slot", slotPath, iron))
				.build();
		ResolutionContext context = new ResolutionContext(iron, head);

		assertTrue(new InSlotTypeCondition(id("forgero:in_slot_type"), slotPath).test(context),
				"Full-path condition should match the slot type exactly");
		assertFalse(new InSlotTypeCondition(id("forgero:in_slot_type"), collidingPath).test(context),
				"A different type sharing the final segment must NOT match");
		assertFalse(new InSlotTypeCondition(id("forgero:in_slot_type"), lastSegment).test(context),
				"The bare final segment must NOT match a multi-segment slot type");
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
