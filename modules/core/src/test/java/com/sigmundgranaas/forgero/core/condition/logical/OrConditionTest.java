package com.sigmundgranaas.forgero.core.condition.logical;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.condition.api.Condition;
import com.sigmundgranaas.forgero.core.condition.api.DynamicCondition;
import com.sigmundgranaas.forgero.core.condition.api.StaticCondition;
import com.sigmundgranaas.forgero.core.property.context.DynamicContext;
import com.sigmundgranaas.forgero.core.property.context.ResolutionContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrConditionTest {

	// Test helper classes
	private static final OpenIdentifier TEST_TYPE = new OpenIdentifier("forgero", "test");

	private record TestStaticCondition(boolean result) implements StaticCondition {
		@Override
		public boolean test(ResolutionContext context) {
			return result;
		}

		@Override
		public OpenIdentifier type() {
			return TEST_TYPE;
		}
	}

	private static class TrackingStaticCondition implements StaticCondition {
		private final boolean result;
		private boolean wasCalled = false;

		TrackingStaticCondition(boolean result) {
			this.result = result;
		}

		@Override
		public boolean test(ResolutionContext context) {
			wasCalled = true;
			return result;
		}

		@Override
		public OpenIdentifier type() {
			return TEST_TYPE;
		}

		boolean wasCalled() {
			return wasCalled;
		}
	}

	private record TestDynamicCondition(boolean result) implements DynamicCondition {
		@Override
		public boolean test(DynamicContext context) {
			return result;
		}

		@Override
		public OpenIdentifier type() {
			return TEST_TYPE;
		}
	}

	private static class TrackingDynamicCondition implements DynamicCondition {
		private final boolean result;
		private boolean wasCalled = false;

		TrackingDynamicCondition(boolean result) {
			this.result = result;
		}

		@Override
		public boolean test(DynamicContext context) {
			wasCalled = true;
			return result;
		}

		@Override
		public OpenIdentifier type() {
			return TEST_TYPE;
		}

		boolean wasCalled() {
			return wasCalled;
		}
	}

	@Nested
	@DisplayName("OrStatic")
	class OrStaticTests {

		@Test
		void returnsTrueWhenAnyConditionIsTrue() {
			StaticCondition alwaysTrue = new TestStaticCondition(true);
			StaticCondition alwaysFalse = new TestStaticCondition(false);

			OrCondition.OrStatic or = new OrCondition.OrStatic(List.of(alwaysFalse, alwaysTrue));

			assertTrue(or.test(null));
		}

		@Test
		void returnsTrueWhenAllConditionsAreTrue() {
			StaticCondition alwaysTrue1 = new TestStaticCondition(true);
			StaticCondition alwaysTrue2 = new TestStaticCondition(true);

			OrCondition.OrStatic or = new OrCondition.OrStatic(List.of(alwaysTrue1, alwaysTrue2));

			assertTrue(or.test(null));
		}

		@Test
		void returnsFalseWhenAllConditionsAreFalse() {
			StaticCondition alwaysFalse1 = new TestStaticCondition(false);
			StaticCondition alwaysFalse2 = new TestStaticCondition(false);

			OrCondition.OrStatic or = new OrCondition.OrStatic(List.of(alwaysFalse1, alwaysFalse2));

			assertFalse(or.test(null));
		}

		@Test
		void handlesEmptyConditionList() {
			OrCondition.OrStatic or = new OrCondition.OrStatic(List.of());

			// Empty OR should return false
			assertFalse(or.test(null));
		}

		@Test
		void shortCircuitsOnFirstTrue() {
			TrackingStaticCondition alwaysTrue = new TrackingStaticCondition(true);
			TrackingStaticCondition trackingCondition = new TrackingStaticCondition(false);

			OrCondition.OrStatic or = new OrCondition.OrStatic(List.of(alwaysTrue, trackingCondition));

			assertTrue(or.test(null));
			assertFalse(trackingCondition.wasCalled(), "OR should short-circuit and not evaluate second condition");
		}

		@Test
		void hasCorrectType() {
			OrCondition.OrStatic or = new OrCondition.OrStatic(List.of());

			assertEquals(OrCondition.TYPE, or.type());
		}
	}

	@Nested
	@DisplayName("OrDynamic")
	class OrDynamicTests {

		@Test
		void returnsTrueWhenAnyDynamicConditionIsTrue() {
			DynamicCondition alwaysTrue = new TestDynamicCondition(true);
			DynamicCondition alwaysFalse = new TestDynamicCondition(false);

			OrCondition.OrDynamic or = new OrCondition.OrDynamic(List.of(), List.of(alwaysFalse, alwaysTrue));

			assertTrue(or.test(DynamicContext.empty()));
		}

		@Test
		void returnsFalseWhenAllDynamicConditionsAreFalse() {
			DynamicCondition alwaysFalse1 = new TestDynamicCondition(false);
			DynamicCondition alwaysFalse2 = new TestDynamicCondition(false);

			OrCondition.OrDynamic or = new OrCondition.OrDynamic(List.of(), List.of(alwaysFalse1, alwaysFalse2));

			assertFalse(or.test(DynamicContext.empty()));
		}

		@Test
		void handlesEmptyDynamicConditionList() {
			OrCondition.OrDynamic or = new OrCondition.OrDynamic(List.of(), List.of());

			// Empty OR should return false
			assertFalse(or.test(DynamicContext.empty()));
		}

		@Test
		void shortCircuitsOnFirstTrueDynamic() {
			TrackingDynamicCondition alwaysTrue = new TrackingDynamicCondition(true);
			TrackingDynamicCondition trackingCondition = new TrackingDynamicCondition(false);

			OrCondition.OrDynamic or = new OrCondition.OrDynamic(List.of(), List.of(alwaysTrue, trackingCondition));

			assertTrue(or.test(DynamicContext.empty()));
			assertFalse(trackingCondition.wasCalled(), "OR should short-circuit on dynamic conditions");
		}

		@Test
		void hasCorrectType() {
			OrCondition.OrDynamic or = new OrCondition.OrDynamic(List.of(), List.of());

			assertEquals(OrCondition.TYPE, or.type());
		}
	}

	@Nested
	@DisplayName("Factory Method")
	class FactoryTests {

		@Test
		void createsOrStaticWhenAllChildrenAreStatic() {
			StaticCondition staticCond = new TestStaticCondition(true);
			Condition condition = new Condition(List.of(staticCond), List.of());

			LogicalConditionResult result = OrCondition.from(condition);

			assertInstanceOf(OrCondition.OrStatic.class, result);
		}

		@Test
		void createsOrDynamicWhenAnyChildIsDynamic() {
			StaticCondition staticCond = new TestStaticCondition(true);
			DynamicCondition dynamicCond = new TestDynamicCondition(true);
			Condition condition = new Condition(List.of(staticCond), List.of(dynamicCond));

			LogicalConditionResult result = OrCondition.from(condition);

			assertInstanceOf(OrCondition.OrDynamic.class, result);
		}

		@Test
		void createsOrStaticForEmptyDynamicList() {
			Condition condition = new Condition(List.of(), List.of());

			LogicalConditionResult result = OrCondition.from(condition);

			assertInstanceOf(OrCondition.OrStatic.class, result);
		}
	}
}
