package com.sigmundgranaas.forgero.core.condition.predicate;

import com.sigmundgranaas.forgero.core.ForgeroTest;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.property.compilation.ResolutionContext;
import org.junit.jupiter.api.Test;

import static com.sigmundgranaas.forgero.testutils.ForgeroTestFactory.*;
import static com.sigmundgranaas.forgero.testutils.TestIdentifiers.*;
import static org.junit.jupiter.api.Assertions.*;

class AtDepthConditionTest extends ForgeroTest {

	@Test
	void conditionPassesForRootAtDepthZero() {
		Component pickaxe = part(PICKAXE_ID).build();

		AtDepthCondition condition = new AtDepthCondition(id("forgero:at_depth"), 0);

		ResolutionContext context = new ResolutionContext(pickaxe, pickaxe);

		assertTrue(condition.test(context), "Root component should be at depth 0");
	}

	@Test
	void conditionPassesForDirectChildAtDepthOne() {
		Component iron = material(IRON_ID, METAL_TAG);
		Component pickaxeHead = part(PICKAXE_HEAD_ID)
				.withStructureSlot(structureSlot("material_slot", TOOL_MATERIAL_ID, iron))
				.build();

		AtDepthCondition condition = new AtDepthCondition(id("forgero:at_depth"), 1);

		ResolutionContext context = new ResolutionContext(iron, pickaxeHead);

		assertTrue(condition.test(context), "Direct child should be at depth 1");
	}

	@Test
	void conditionFailsWhenDepthDoesNotMatch() {
		Component iron = material(IRON_ID, METAL_TAG);
		Component pickaxeHead = part(PICKAXE_HEAD_ID)
				.withStructureSlot(structureSlot("material_slot", TOOL_MATERIAL_ID, iron))
				.build();

		AtDepthCondition condition = new AtDepthCondition(id("forgero:at_depth"), 0);

		ResolutionContext context = new ResolutionContext(iron, pickaxeHead);

		assertFalse(condition.test(context), "Depth 1 component should not match depth 0 condition");
	}

	@Test
	void conditionPassesForNestedComponentAtDepthTwo() {
		Component iron = material(IRON_ID, METAL_TAG);
		Component pickaxeHead = part(PICKAXE_HEAD_ID)
				.withStructureSlot(structureSlot("material_slot", TOOL_MATERIAL_ID, iron))
				.build();
		Component pickaxe = part(PICKAXE_ID)
				.withStructureSlot(structureSlot("head_slot", PICKAXE_HEAD_ID, pickaxeHead))
				.build();

		AtDepthCondition condition = new AtDepthCondition(id("forgero:at_depth"), 2);

		ResolutionContext context = new ResolutionContext(iron, pickaxe);

		assertTrue(condition.test(context), "Nested component (2 levels deep) should be at depth 2");
	}

	@Test
	void hasCorrectType() {
		AtDepthCondition condition = new AtDepthCondition(id("forgero:at_depth"), 1);

		assertEquals(id("forgero:at_depth"), condition.type());
	}

	@Test
	void hasCorrectDepth() {
		AtDepthCondition condition = new AtDepthCondition(id("forgero:at_depth"), 3);

		assertEquals(3, condition.depth());
	}
}
