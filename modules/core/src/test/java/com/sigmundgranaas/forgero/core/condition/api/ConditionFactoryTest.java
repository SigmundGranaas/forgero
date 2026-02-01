package com.sigmundgranaas.forgero.core.condition.api;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.property.context.DynamicContext;
import com.sigmundgranaas.forgero.core.property.context.ResolutionContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Condition factory methods.
 *
 * <p>This test suite validates the factory methods for creating and combining
 * conditions, including:
 * <ul>
 *   <li>{@link Condition#ofStatic(StaticCondition...)} - static-only conditions</li>
 *   <li>{@link Condition#ofDynamic(DynamicCondition...)} - dynamic-only conditions</li>
 *   <li>{@link Condition#all(Condition...)} - AND semantics for multiple conditions</li>
 *   <li>{@link Condition#all(StaticCondition...)} - AND semantics for static conditions</li>
 *   <li>{@link Condition#all(DynamicCondition...)} - AND semantics for dynamic conditions</li>
 *   <li>{@link Condition#any(Condition...)} - OR semantics for runtime evaluation</li>
 * </ul>
 */
class ConditionFactoryTest {

	// ========================================================================
	// TEST IMPLEMENTATIONS
	// ========================================================================

	/**
	 * Simple test implementation of StaticCondition.
	 *
	 * @param result The boolean result this condition should return
	 */
	record TestStaticCondition(boolean result) implements StaticCondition {
		@Override
		public boolean test(ResolutionContext ctx) {
			return result;
		}

		@Override
		public OpenIdentifier type() {
			return new OpenIdentifier("test", "static");
		}
	}

	/**
	 * Simple test implementation of DynamicCondition.
	 *
	 * @param result The boolean result this condition should return
	 */
	record TestDynamicCondition(boolean result) implements DynamicCondition {
		@Override
		public boolean test(DynamicContext ctx) {
			return result;
		}

		@Override
		public OpenIdentifier type() {
			return new OpenIdentifier("test", "dynamic");
		}
	}

	// ========================================================================
	// ofStatic(...) TESTS
	// ========================================================================

	@Test
	void ofStatic_withEmptyArray_returnsConditionWithNoStaticConditions() {
		Condition condition = Condition.ofStatic();

		assertTrue(condition.hasNoStaticConditions());
		assertTrue(condition.hasNoDynamicConditions());
		assertTrue(condition.isAlwaysTrue());
	}

	@Test
	void ofStatic_withSingleCondition_returnsConditionWithOneStatic() {
		StaticCondition sc = new TestStaticCondition(true);
		Condition condition = Condition.ofStatic(sc);

		assertEquals(1, condition.staticConditions().size());
		assertEquals(sc, condition.staticConditions().get(0));
		assertTrue(condition.hasNoDynamicConditions());
	}

	@Test
	void ofStatic_withMultipleConditions_returnsConditionWithAllStatic() {
		StaticCondition sc1 = new TestStaticCondition(true);
		StaticCondition sc2 = new TestStaticCondition(false);
		StaticCondition sc3 = new TestStaticCondition(true);
		Condition condition = Condition.ofStatic(sc1, sc2, sc3);

		assertEquals(3, condition.staticConditions().size());
		assertTrue(condition.staticConditions().contains(sc1));
		assertTrue(condition.staticConditions().contains(sc2));
		assertTrue(condition.staticConditions().contains(sc3));
		assertTrue(condition.hasNoDynamicConditions());
	}

	// ========================================================================
	// ofDynamic(...) TESTS
	// ========================================================================

	@Test
	void ofDynamic_withEmptyArray_returnsConditionWithNoDynamicConditions() {
		Condition condition = Condition.ofDynamic();

		assertTrue(condition.hasNoStaticConditions());
		assertTrue(condition.hasNoDynamicConditions());
		assertTrue(condition.isAlwaysTrue());
	}

	@Test
	void ofDynamic_withSingleCondition_returnsConditionWithOneDynamic() {
		DynamicCondition dc = new TestDynamicCondition(true);
		Condition condition = Condition.ofDynamic(dc);

		assertEquals(1, condition.dynamicConditions().size());
		assertEquals(dc, condition.dynamicConditions().get(0));
		assertTrue(condition.hasNoStaticConditions());
	}

	@Test
	void ofDynamic_withMultipleConditions_returnsConditionWithAllDynamic() {
		DynamicCondition dc1 = new TestDynamicCondition(true);
		DynamicCondition dc2 = new TestDynamicCondition(false);
		DynamicCondition dc3 = new TestDynamicCondition(true);
		Condition condition = Condition.ofDynamic(dc1, dc2, dc3);

		assertEquals(3, condition.dynamicConditions().size());
		assertTrue(condition.dynamicConditions().contains(dc1));
		assertTrue(condition.dynamicConditions().contains(dc2));
		assertTrue(condition.dynamicConditions().contains(dc3));
		assertTrue(condition.hasNoStaticConditions());
	}

	// ========================================================================
	// all(Condition...) TESTS
	// ========================================================================

	@Test
	void all_withEmptyConditions_returnsAlwaysTrue() {
		Condition condition = Condition.all(new Condition[0]);

		assertSame(Condition.ALWAYS_TRUE, condition);
	}

	@Test
	void all_withSingleCondition_returnsSameInstance() {
		Condition original = Condition.ofStatic(new TestStaticCondition(true));
		Condition result = Condition.all(original);

		assertSame(original, result);
	}

	@Test
	void all_withMultipleConditions_mergesAllStaticAndDynamic() {
		StaticCondition sc1 = new TestStaticCondition(true);
		StaticCondition sc2 = new TestStaticCondition(false);
		DynamicCondition dc1 = new TestDynamicCondition(true);
		DynamicCondition dc2 = new TestDynamicCondition(false);

		Condition cond1 = Condition.ofStatic(sc1);
		Condition cond2 = Condition.ofDynamic(dc1);
		Condition cond3 = new Condition(java.util.List.of(sc2), java.util.List.of(dc2));

		Condition result = Condition.all(cond1, cond2, cond3);

		assertEquals(2, result.staticConditions().size());
		assertTrue(result.staticConditions().contains(sc1));
		assertTrue(result.staticConditions().contains(sc2));

		assertEquals(2, result.dynamicConditions().size());
		assertTrue(result.dynamicConditions().contains(dc1));
		assertTrue(result.dynamicConditions().contains(dc2));
	}

	@Test
	void all_withOnlyStaticConditions_mergesCorrectly() {
		StaticCondition sc1 = new TestStaticCondition(true);
		StaticCondition sc2 = new TestStaticCondition(false);

		Condition cond1 = Condition.ofStatic(sc1);
		Condition cond2 = Condition.ofStatic(sc2);

		Condition result = Condition.all(cond1, cond2);

		assertEquals(2, result.staticConditions().size());
		assertTrue(result.hasNoDynamicConditions());
	}

	@Test
	void all_withOnlyDynamicConditions_mergesCorrectly() {
		DynamicCondition dc1 = new TestDynamicCondition(true);
		DynamicCondition dc2 = new TestDynamicCondition(false);

		Condition cond1 = Condition.ofDynamic(dc1);
		Condition cond2 = Condition.ofDynamic(dc2);

		Condition result = Condition.all(cond1, cond2);

		assertEquals(2, result.dynamicConditions().size());
		assertTrue(result.hasNoStaticConditions());
	}

	// ========================================================================
	// all(StaticCondition...) TESTS
	// ========================================================================

	@Test
	void all_withStaticConditionVarargs_delegatesToOfStatic() {
		StaticCondition sc1 = new TestStaticCondition(true);
		StaticCondition sc2 = new TestStaticCondition(false);

		Condition result = Condition.all(sc1, sc2);

		// Should be equivalent to ofStatic(sc1, sc2)
		assertEquals(2, result.staticConditions().size());
		assertTrue(result.staticConditions().contains(sc1));
		assertTrue(result.staticConditions().contains(sc2));
		assertTrue(result.hasNoDynamicConditions());
	}

	@Test
	void all_withEmptyStaticConditionVarargs_returnsEmptyStatic() {
		Condition result = Condition.all(new StaticCondition[0]);

		assertTrue(result.hasNoStaticConditions());
		assertTrue(result.hasNoDynamicConditions());
	}

	// ========================================================================
	// all(DynamicCondition...) TESTS
	// ========================================================================

	@Test
	void all_withDynamicConditionVarargs_delegatesToOfDynamic() {
		DynamicCondition dc1 = new TestDynamicCondition(true);
		DynamicCondition dc2 = new TestDynamicCondition(false);

		Condition result = Condition.all(dc1, dc2);

		// Should be equivalent to ofDynamic(dc1, dc2)
		assertEquals(2, result.dynamicConditions().size());
		assertTrue(result.dynamicConditions().contains(dc1));
		assertTrue(result.dynamicConditions().contains(dc2));
		assertTrue(result.hasNoStaticConditions());
	}

	@Test
	void all_withEmptyDynamicConditionVarargs_returnsEmptyDynamic() {
		Condition result = Condition.all(new DynamicCondition[0]);

		assertTrue(result.hasNoStaticConditions());
		assertTrue(result.hasNoDynamicConditions());
	}

	// ========================================================================
	// any(Condition...) TESTS
	// ========================================================================

	@Test
	void any_withEmptyConditions_returnsAlwaysTrue() {
		Condition condition = Condition.any(new Condition[0]);

		assertSame(Condition.ALWAYS_TRUE, condition);
	}

	@Test
	void any_withSingleCondition_returnsSameInstance() {
		Condition original = Condition.ofDynamic(new TestDynamicCondition(true));
		Condition result = Condition.any(original);

		assertSame(original, result);
	}

	@Test
	void any_withMultipleConditions_mergesStaticAndWrapsInOrDynamic() {
		StaticCondition sc1 = new TestStaticCondition(true);
		StaticCondition sc2 = new TestStaticCondition(false);
		DynamicCondition dc1 = new TestDynamicCondition(true);
		DynamicCondition dc2 = new TestDynamicCondition(false);

		Condition cond1 = new Condition(java.util.List.of(sc1), java.util.List.of(dc1));
		Condition cond2 = new Condition(java.util.List.of(sc2), java.util.List.of(dc2));

		Condition result = Condition.any(cond1, cond2);

		// All static conditions should be merged (AND semantics during baking)
		assertEquals(2, result.staticConditions().size());
		assertTrue(result.staticConditions().contains(sc1));
		assertTrue(result.staticConditions().contains(sc2));

		// Should have exactly one dynamic condition (the OrDynamicCondition wrapper)
		assertEquals(1, result.dynamicConditions().size());
		// Can't check OrDynamicCondition type directly since it's private,
		// but we can verify it's a DynamicCondition
		assertNotNull(result.dynamicConditions().get(0));
	}

	@Test
	void any_withOnlyDynamicConditions_wrapsInOrDynamic() {
		DynamicCondition dc1 = new TestDynamicCondition(true);
		DynamicCondition dc2 = new TestDynamicCondition(false);

		Condition cond1 = Condition.ofDynamic(dc1);
		Condition cond2 = Condition.ofDynamic(dc2);

		Condition result = Condition.any(cond1, cond2);

		assertTrue(result.hasNoStaticConditions());
		assertEquals(1, result.dynamicConditions().size());
		// Can't check OrDynamicCondition type directly since it's private,
		// but we can verify it's a DynamicCondition
		assertNotNull(result.dynamicConditions().get(0));
	}

	@Test
	void any_withStaticOnlyCondition_setsHasStaticOnlyConditionFlag() {
		StaticCondition sc1 = new TestStaticCondition(true);
		DynamicCondition dc1 = new TestDynamicCondition(true);

		Condition staticOnly = Condition.ofStatic(sc1);
		Condition withDynamic = Condition.ofDynamic(dc1);

		Condition result = Condition.any(staticOnly, withDynamic);

		// Should have merged static conditions
		assertEquals(1, result.staticConditions().size());

		// Should have OrDynamicCondition wrapper
		assertEquals(1, result.dynamicConditions().size());
		// Verify behavior: should pass because static-only condition counts as "always true at runtime"
		DynamicCondition orCondition = result.dynamicConditions().get(0);
		assertTrue(orCondition.test(DynamicContext.empty()));
	}

	@Test
	void any_withNoDynamicConditions_handlesStaticOnlyConditions() {
		StaticCondition sc1 = new TestStaticCondition(true);
		StaticCondition sc2 = new TestStaticCondition(false);

		Condition cond1 = Condition.ofStatic(sc1);
		Condition cond2 = Condition.ofStatic(sc2);

		Condition result = Condition.any(cond1, cond2);

		// Static conditions are merged
		assertEquals(2, result.staticConditions().size());

		// OrDynamicCondition should be created
		assertEquals(1, result.dynamicConditions().size());
		// Verify behavior: should pass because static-only conditions count as "always true at runtime"
		DynamicCondition orCondition = result.dynamicConditions().get(0);
		assertTrue(orCondition.test(DynamicContext.empty()));
	}

	// ========================================================================
	// OrDynamicCondition BEHAVIOR TESTS
	// ========================================================================

	@Test
	void orDynamicCondition_withAllFalseDynamic_returnsFalse() {
		DynamicCondition dc1 = new TestDynamicCondition(false);
		DynamicCondition dc2 = new TestDynamicCondition(false);

		Condition cond1 = Condition.ofDynamic(dc1);
		Condition cond2 = Condition.ofDynamic(dc2);

		Condition result = Condition.any(cond1, cond2);

		// Test the OrDynamicCondition
		DynamicCondition orCondition = result.dynamicConditions().get(0);
		assertFalse(orCondition.test(DynamicContext.empty()));
	}

	@Test
	void orDynamicCondition_withOneTrueDynamic_returnsTrue() {
		DynamicCondition dc1 = new TestDynamicCondition(false);
		DynamicCondition dc2 = new TestDynamicCondition(true);

		Condition cond1 = Condition.ofDynamic(dc1);
		Condition cond2 = Condition.ofDynamic(dc2);

		Condition result = Condition.any(cond1, cond2);

		DynamicCondition orCondition = result.dynamicConditions().get(0);
		assertTrue(orCondition.test(DynamicContext.empty()));
	}

	@Test
	void orDynamicCondition_withNoDynamicConditionsInInnerCondition_returnsTrue() {
		// If an inner condition has no dynamic conditions, it means static conditions
		// passed during baking, so it should count as "always true at runtime"
		StaticCondition sc1 = new TestStaticCondition(true);
		DynamicCondition dc1 = new TestDynamicCondition(false);

		Condition staticOnly = Condition.ofStatic(sc1);
		Condition withDynamic = Condition.ofDynamic(dc1);

		Condition result = Condition.any(staticOnly, withDynamic);

		DynamicCondition orCondition = result.dynamicConditions().get(0);
		// Should return true because staticOnly has no dynamic conditions
		// (meaning its static conditions passed during baking)
		assertTrue(orCondition.test(DynamicContext.empty()));
	}

	@Test
	void orDynamicCondition_withAlwaysTrueCondition_returnsTrue() {
		Condition alwaysTrue = Condition.ALWAYS_TRUE;
		DynamicCondition dc1 = new TestDynamicCondition(false);

		Condition withDynamic = Condition.ofDynamic(dc1);

		Condition result = Condition.any(alwaysTrue, withDynamic);

		DynamicCondition orCondition = result.dynamicConditions().get(0);
		// Should return true because ALWAYS_TRUE has no dynamic conditions
		assertTrue(orCondition.test(DynamicContext.empty()));
	}

	@Test
	void orDynamicCondition_type_returnsCorrectIdentifier() {
		DynamicCondition dc1 = new TestDynamicCondition(true);
		DynamicCondition dc2 = new TestDynamicCondition(true);

		Condition cond1 = Condition.ofDynamic(dc1);
		Condition cond2 = Condition.ofDynamic(dc2);

		Condition result = Condition.any(cond1, cond2);

		DynamicCondition orCondition = result.dynamicConditions().get(0);
		assertEquals(new OpenIdentifier("forgero", "condition/or"), orCondition.type());
	}

	// ========================================================================
	// IMMUTABILITY TESTS
	// ========================================================================

	@Test
	void condition_isImmutable_staticListCopyDefense() {
		StaticCondition[] original = {new TestStaticCondition(true)};
		Condition condition = Condition.ofStatic(original);

		// Mutate the original array
		original[0] = new TestStaticCondition(false);

		// Condition should not be affected
		assertTrue(((TestStaticCondition) condition.staticConditions().get(0)).result());
	}

	@Test
	void condition_isImmutable_dynamicListCopyDefense() {
		DynamicCondition[] original = {new TestDynamicCondition(true)};
		Condition condition = Condition.ofDynamic(original);

		// Mutate the original array
		original[0] = new TestDynamicCondition(false);

		// Condition should not be affected
		assertTrue(((TestDynamicCondition) condition.dynamicConditions().get(0)).result());
	}

	// ========================================================================
	// ALWAYSTRUE CONSTANT TESTS
	// ========================================================================

	@Test
	void alwaysTrue_hasNoConditions() {
		assertTrue(Condition.ALWAYS_TRUE.hasNoStaticConditions());
		assertTrue(Condition.ALWAYS_TRUE.hasNoDynamicConditions());
		assertTrue(Condition.ALWAYS_TRUE.isAlwaysTrue());
	}

	@Test
	void alwaysTrue_staticConditionsIsEmpty() {
		assertTrue(Condition.ALWAYS_TRUE.staticConditions().isEmpty());
	}

	@Test
	void alwaysTrue_dynamicConditionsIsEmpty() {
		assertTrue(Condition.ALWAYS_TRUE.dynamicConditions().isEmpty());
	}
}
