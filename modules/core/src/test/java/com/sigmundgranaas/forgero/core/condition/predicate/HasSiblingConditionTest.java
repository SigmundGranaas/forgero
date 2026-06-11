package com.sigmundgranaas.forgero.core.condition.predicate;

import com.sigmundgranaas.forgero.core.ForgeroTest;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.property.compilation.ResolutionContext;
import org.junit.jupiter.api.Test;

import static com.sigmundgranaas.forgero.testutils.ForgeroTestFactory.*;
import static com.sigmundgranaas.forgero.testutils.TestIdentifiers.*;
import static org.junit.jupiter.api.Assertions.*;

class HasSiblingConditionTest extends ForgeroTest {

	@Test
	void conditionPassesWhenSiblingExists() {
		Component pickaxeHead = part(PICKAXE_HEAD_ID).build();
		Component handle = material(HANDLE_ID, WOOD_TAG);
		Component pickaxe = part(PICKAXE_ID)
				.withStructureSlot(structureSlot("head_slot", PICKAXE_HEAD_ID, pickaxeHead))
				.withStructureSlot(structureSlot("handle_slot", HANDLE_ID, handle))
				.build();

		HasSiblingCondition condition = new HasSiblingCondition(id("forgero:has_sibling"), HANDLE_ID);

		ResolutionContext context = new ResolutionContext(pickaxeHead, pickaxe);

		assertTrue(condition.test(context), "Condition should pass when sibling with specified ID exists");
	}

	@Test
	void conditionFailsWhenSiblingDoesNotExist() {
		Component pickaxeHead = part(PICKAXE_HEAD_ID).build();
		Component pickaxe = part(PICKAXE_ID)
				.withStructureSlot(structureSlot("head_slot", PICKAXE_HEAD_ID, pickaxeHead))
				.build();

		HasSiblingCondition condition = new HasSiblingCondition(id("forgero:has_sibling"), HANDLE_ID);

		ResolutionContext context = new ResolutionContext(pickaxeHead, pickaxe);

		assertFalse(condition.test(context), "Condition should fail when sibling with specified ID does not exist");
	}

	@Test
	void conditionFailsForRootComponent() {
		Component pickaxe = part(PICKAXE_ID).build();

		HasSiblingCondition condition = new HasSiblingCondition(id("forgero:has_sibling"), HANDLE_ID);

		ResolutionContext context = new ResolutionContext(pickaxe, pickaxe);

		assertFalse(condition.test(context), "Condition should fail for root component (no siblings)");
	}

	@Test
	void conditionDoesNotMatchSelf() {
		Component pickaxeHead = part(PICKAXE_HEAD_ID).build();
		Component handle = material(HANDLE_ID, WOOD_TAG);
		Component pickaxe = part(PICKAXE_ID)
				.withStructureSlot(structureSlot("head_slot", PICKAXE_HEAD_ID, pickaxeHead))
				.withStructureSlot(structureSlot("handle_slot", HANDLE_ID, handle))
				.build();

		HasSiblingCondition condition = new HasSiblingCondition(id("forgero:has_sibling"), PICKAXE_HEAD_ID);

		ResolutionContext context = new ResolutionContext(pickaxeHead, pickaxe);

		// The component should not consider itself a sibling
		assertFalse(condition.test(context), "Condition should not match the component itself");
	}

	@Test
	void hasCorrectType() {
		HasSiblingCondition condition = new HasSiblingCondition(id("forgero:has_sibling"), HANDLE_ID);

		assertEquals(id("forgero:has_sibling"), condition.type());
	}

	@Test
	void hasCorrectSiblingId() {
		HasSiblingCondition condition = new HasSiblingCondition(id("forgero:has_sibling"), HANDLE_ID);

		assertEquals(HANDLE_ID, condition.siblingId());
	}
}
