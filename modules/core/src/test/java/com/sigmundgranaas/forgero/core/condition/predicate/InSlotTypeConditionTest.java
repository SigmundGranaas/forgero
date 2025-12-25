package com.sigmundgranaas.forgero.core.condition.predicate;

import com.sigmundgranaas.forgero.core.ForgeroTest;
import com.sigmundgranaas.forgero.core.component.api.structure.ComponentStructure;
import com.sigmundgranaas.forgero.core.component.impl.StaticComponent;
import com.sigmundgranaas.forgero.core.component.impl.StructuredPart;
import com.sigmundgranaas.forgero.core.property.context.ResolutionContext;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class InSlotTypeConditionTest extends ForgeroTest {

	@Test
	void conditionPassesWhenComponentIsInMatchingSlot() {
		StaticComponent iron = material(IRON_ID, METAL_TAG);
		StructuredPart pickaxeHead = new StructuredPart(
				PICKAXE_HEAD_ID,
				Set.of(),
				new HashMap<>(),
				new ComponentStructure(slotsMap(
						slot(idFactory.of("material_slot"), TOOL_MATERIAL_ID, iron)
				))
		);

		InSlotTypeCondition condition = new InSlotTypeCondition(
				idFactory.of("forgero:in_slot_type"),
				TOOL_MATERIAL_ID  // when_in: material
		);

		ResolutionContext context = new ResolutionContext(iron, pickaxeHead);

		assertTrue(condition.test(context), "Condition should pass when component is in matching slot type");
	}

	@Test
	void conditionFailsWhenComponentIsInDifferentSlot() {
		StaticComponent iron = material(IRON_ID, METAL_TAG);
		StructuredPart pickaxeHead = new StructuredPart(
				PICKAXE_HEAD_ID,
				Set.of(),
				new HashMap<>(),
				new ComponentStructure(slotsMap(
						slot(idFactory.of("material_slot"), TOOL_MATERIAL_ID, iron)
				))
		);

		InSlotTypeCondition condition = new InSlotTypeCondition(
				idFactory.of("forgero:in_slot_type"),
				idFactory.of("forgero:upgrade")  // when_in: upgrade (wrong slot type)
		);

		ResolutionContext context = new ResolutionContext(iron, pickaxeHead);

		assertFalse(condition.test(context), "Condition should fail when component is in different slot type");
	}

	@Test
	void conditionFailsWhenComponentIsRoot() {
		StructuredPart pickaxeHead = new StructuredPart(
				PICKAXE_HEAD_ID,
				Set.of(),
				new HashMap<>(),
				new ComponentStructure(slotsMap())
		);

		InSlotTypeCondition condition = new InSlotTypeCondition(
				idFactory.of("forgero:in_slot_type"),
				TOOL_MATERIAL_ID
		);

		ResolutionContext context = new ResolutionContext(pickaxeHead, pickaxeHead);

		assertFalse(condition.test(context), "Condition should fail when component is root (not in any slot)");
	}
}
