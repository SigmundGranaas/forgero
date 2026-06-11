package com.sigmundgranaas.forgero.core.condition.predicate;

import com.sigmundgranaas.forgero.core.ForgeroTest;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.property.compilation.ResolutionContext;
import org.junit.jupiter.api.Test;

import static com.sigmundgranaas.forgero.testutils.ForgeroTestFactory.*;
import static com.sigmundgranaas.forgero.testutils.TestIdentifiers.*;
import static org.junit.jupiter.api.Assertions.*;

class IsRootConditionTest extends ForgeroTest {

	@Test
	void conditionPassesWhenComponentIsRoot() {
		Component pickaxe = part(PICKAXE_ID).build();

		IsRootCondition condition = new IsRootCondition(id("forgero:is_root"));

		ResolutionContext context = new ResolutionContext(pickaxe, pickaxe);

		assertTrue(condition.test(context), "Condition should pass when component is root (component == parent)");
	}

	@Test
	void conditionFailsWhenComponentIsNotRoot() {
		Component iron = material(IRON_ID, METAL_TAG);
		Component pickaxeHead = part(PICKAXE_HEAD_ID)
				.withStructureSlot(structureSlot("material_slot", TOOL_MATERIAL_ID, iron))
				.build();

		IsRootCondition condition = new IsRootCondition(id("forgero:is_root"));

		ResolutionContext context = new ResolutionContext(iron, pickaxeHead);

		assertFalse(condition.test(context), "Condition should fail when component is not root (has parent)");
	}

	@Test
	void conditionFailsForNestedComponent() {
		Component iron = material(IRON_ID, METAL_TAG);
		Component pickaxeHead = part(PICKAXE_HEAD_ID)
				.withStructureSlot(structureSlot("material_slot", TOOL_MATERIAL_ID, iron))
				.build();
		Component pickaxe = part(PICKAXE_ID)
				.withStructureSlot(structureSlot("head_slot", PICKAXE_HEAD_ID, pickaxeHead))
				.build();

		IsRootCondition condition = new IsRootCondition(id("forgero:is_root"));

		ResolutionContext context = new ResolutionContext(iron, pickaxe);

		assertFalse(condition.test(context), "Condition should fail for deeply nested component");
	}

	@Test
	void hasCorrectType() {
		IsRootCondition condition = new IsRootCondition(id("forgero:is_root"));

		assertEquals(id("forgero:is_root"), condition.type());
	}
}
