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

class AndConditionTest {

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
	@DisplayName("AndStatic")
	class AndStaticTests {

		@Test
		void returnsTrueWhenAllConditionsAreTrue() {
			StaticCondition alwaysTrue1 = new TestStaticCondition(true);
			StaticCondition alwaysTrue2 = new TestStaticCondition(true);

			AndCondition.AndStatic and = new AndCondition.AndStatic(List.of(alwaysTrue1, alwaysTrue2));

			assertTrue(and.test(null));
		}

		@Test
		void returnsFalseWhenAnyConditionIsFalse() {
			StaticCondition alwaysTrue = new TestStaticCondition(true);
			StaticCondition alwaysFalse = new TestStaticCondition(false);

			AndCondition.AndStatic and = new AndCondition.AndStatic(List.of(alwaysTrue, alwaysFalse));

			assertFalse(and.test(null));
		}

		@Test
		void returnsFalseWhenAllConditionsAreFalse() {
			StaticCondition alwaysFalse1 = new TestStaticCondition(false);
			StaticCondition alwaysFalse2 = new TestStaticCondition(false);

			AndCondition.AndStatic and = new AndCondition.AndStatic(List.of(alwaysFalse1, alwaysFalse2));

			assertFalse(and.test(null));
		}

		@Test
		void handlesEmptyConditionList() {
			AndCondition.AndStatic and = new AndCondition.AndStatic(List.of());

			// Empty AND should return true (vacuous truth)
			assertTrue(and.test(null));
		}

		@Test
		void shortCircuitsOnFirstFalse() {
			TrackingStaticCondition alwaysFalse = new TrackingStaticCondition(false);
			TrackingStaticCondition trackingCondition = new TrackingStaticCondition(true);

			AndCondition.AndStatic and = new AndCondition.AndStatic(List.of(alwaysFalse, trackingCondition));

			assertFalse(and.test(null));
			assertFalse(trackingCondition.wasCalled(), "AND should short-circuit and not evaluate second condition");
		}

		@Test
		void hasCorrectType() {
			AndCondition.AndStatic and = new AndCondition.AndStatic(List.of());

			assertEquals(AndCondition.TYPE, and.type());
		}
	}

	@Nested
	@DisplayName("AndDynamic")
	class AndDynamicTests {

		@Test
		void returnsTrueWhenAllDynamicConditionsAreTrue() {
			DynamicCondition alwaysTrue1 = new TestDynamicCondition(true);
			DynamicCondition alwaysTrue2 = new TestDynamicCondition(true);

			AndCondition.AndDynamic and = new AndCondition.AndDynamic(List.of(), List.of(alwaysTrue1, alwaysTrue2));

			assertTrue(and.test(DynamicContext.empty()));
		}

		@Test
		void returnsFalseWhenAnyDynamicConditionIsFalse() {
			DynamicCondition alwaysTrue = new TestDynamicCondition(true);
			DynamicCondition alwaysFalse = new TestDynamicCondition(false);

			AndCondition.AndDynamic and = new AndCondition.AndDynamic(List.of(), List.of(alwaysTrue, alwaysFalse));

			assertFalse(and.test(DynamicContext.empty()));
		}

		@Test
		void handlesEmptyDynamicConditionList() {
			AndCondition.AndDynamic and = new AndCondition.AndDynamic(List.of(), List.of());

			// Empty AND should return true (vacuous truth)
			assertTrue(and.test(DynamicContext.empty()));
		}

		@Test
		void shortCircuitsOnFirstFalseDynamic() {
			TrackingDynamicCondition alwaysFalse = new TrackingDynamicCondition(false);
			TrackingDynamicCondition trackingCondition = new TrackingDynamicCondition(true);

			AndCondition.AndDynamic and = new AndCondition.AndDynamic(List.of(), List.of(alwaysFalse, trackingCondition));

			assertFalse(and.test(DynamicContext.empty()));
			assertFalse(trackingCondition.wasCalled(), "AND should short-circuit on dynamic conditions");
		}

		@Test
		void hasCorrectType() {
			AndCondition.AndDynamic and = new AndCondition.AndDynamic(List.of(), List.of());

			assertEquals(AndCondition.TYPE, and.type());
		}
	}

	@Nested
	@DisplayName("Factory Method")
	class FactoryTests {

		@Test
		void createsAndStaticWhenAllChildrenAreStatic() {
			StaticCondition staticCond = new TestStaticCondition(true);
			Condition condition = new Condition(List.of(staticCond), List.of());

			LogicalConditionResult result = AndCondition.from(condition);

			assertInstanceOf(AndCondition.AndStatic.class, result);
		}

		@Test
		void createsAndDynamicWhenAnyChildIsDynamic() {
			StaticCondition staticCond = new TestStaticCondition(true);
			DynamicCondition dynamicCond = new TestDynamicCondition(true);
			Condition condition = new Condition(List.of(staticCond), List.of(dynamicCond));

			LogicalConditionResult result = AndCondition.from(condition);

			assertInstanceOf(AndCondition.AndDynamic.class, result);
		}

		@Test
		void createsAndStaticForEmptyDynamicList() {
			Condition condition = new Condition(List.of(), List.of());

			LogicalConditionResult result = AndCondition.from(condition);

			assertInstanceOf(AndCondition.AndStatic.class, result);
		}
	}
}
