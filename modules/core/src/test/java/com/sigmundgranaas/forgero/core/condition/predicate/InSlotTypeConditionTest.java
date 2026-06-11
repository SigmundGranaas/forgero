package com.sigmundgranaas.forgero.core.condition.predicate;

import com.sigmundgranaas.forgero.core.ForgeroTest;
import com.sigmundgranaas.forgero.core.component.api.Component;
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
}
