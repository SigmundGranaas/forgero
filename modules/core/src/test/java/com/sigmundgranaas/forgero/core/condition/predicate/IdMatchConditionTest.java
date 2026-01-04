package com.sigmundgranaas.forgero.core.condition.predicate;

import com.sigmundgranaas.forgero.core.ForgeroTest;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.property.context.ResolutionContext;
import org.junit.jupiter.api.Test;

import static com.sigmundgranaas.forgero.testutils.ForgeroTestFactory.*;
import static com.sigmundgranaas.forgero.testutils.TestIdentifiers.*;
import static org.junit.jupiter.api.Assertions.*;

class IdMatchConditionTest extends ForgeroTest {

	@Test
	void conditionPassesWhenIdMatches() {
		Component iron = material(IRON_ID, METAL_TAG);

		IdMatchCondition condition = new IdMatchCondition(id("forgero:id_match"), "forgero:iron");

		ResolutionContext context = new ResolutionContext(iron, iron);

		assertTrue(condition.test(context), "Condition should pass when ID matches exactly");
	}

	@Test
	void conditionPassesWhenIdMatchesWithoutNamespace() {
		Component iron = material(IRON_ID, METAL_TAG);

		IdMatchCondition condition = new IdMatchCondition(id("forgero:id_match"), "iron");

		ResolutionContext context = new ResolutionContext(iron, iron);

		assertTrue(condition.test(context), "Condition should pass when ID matches without namespace");
	}

	@Test
	void conditionFailsWhenIdDoesNotMatch() {
		Component iron = material(IRON_ID, METAL_TAG);

		IdMatchCondition condition = new IdMatchCondition(id("forgero:id_match"), "diamond");

		ResolutionContext context = new ResolutionContext(iron, iron);

		assertFalse(condition.test(context), "Condition should fail when ID does not match");
	}

	@Test
	void conditionFailsForPartialMatch() {
		Component iron = material(IRON_ID, METAL_TAG);

		IdMatchCondition condition = new IdMatchCondition(id("forgero:id_match"), "iro");

		ResolutionContext context = new ResolutionContext(iron, iron);

		assertFalse(condition.test(context), "Condition should fail for partial ID match");
	}

	@Test
	void hasCorrectType() {
		IdMatchCondition condition = new IdMatchCondition(id("forgero:id_match"), "iron");

		assertEquals(id("forgero:id_match"), condition.type());
	}

	@Test
	void hasCorrectId() {
		IdMatchCondition condition = new IdMatchCondition(id("forgero:id_match"), "iron");

		assertEquals("iron", condition.id());
	}
}
