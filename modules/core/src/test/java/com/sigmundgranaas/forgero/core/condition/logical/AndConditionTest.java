package com.sigmundgranaas.forgero.core.condition.logical;

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

	@Nested
	@DisplayName("AndStatic")
	class AndStaticTests {

		@Test
		void returnsTrueWhenAllConditionsAreTrue() {
			StaticCondition alwaysTrue1 = ctx -> true;
			StaticCondition alwaysTrue2 = ctx -> true;

			AndCondition.AndStatic and = new AndCondition.AndStatic(List.of(alwaysTrue1, alwaysTrue2));

			assertTrue(and.test(null));
		}

		@Test
		void returnsFalseWhenAnyConditionIsFalse() {
			StaticCondition alwaysTrue = ctx -> true;
			StaticCondition alwaysFalse = ctx -> false;

			AndCondition.AndStatic and = new AndCondition.AndStatic(List.of(alwaysTrue, alwaysFalse));

			assertFalse(and.test(null));
		}

		@Test
		void returnsFalseWhenAllConditionsAreFalse() {
			StaticCondition alwaysFalse1 = ctx -> false;
			StaticCondition alwaysFalse2 = ctx -> false;

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
			// Use a flag to track if second condition is evaluated
			boolean[] secondEvaluated = {false};

			StaticCondition alwaysFalse = ctx -> false;
			StaticCondition trackingCondition = ctx -> {
				secondEvaluated[0] = true;
				return true;
			};

			AndCondition.AndStatic and = new AndCondition.AndStatic(List.of(alwaysFalse, trackingCondition));

			assertFalse(and.test(null));
			assertFalse(secondEvaluated[0], "AND should short-circuit and not evaluate second condition");
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
			DynamicCondition alwaysTrue1 = ctx -> true;
			DynamicCondition alwaysTrue2 = ctx -> true;

			AndCondition.AndDynamic and = new AndCondition.AndDynamic(List.of(), List.of(alwaysTrue1, alwaysTrue2));

			assertTrue(and.test(DynamicContext.empty()));
		}

		@Test
		void returnsFalseWhenAnyDynamicConditionIsFalse() {
			DynamicCondition alwaysTrue = ctx -> true;
			DynamicCondition alwaysFalse = ctx -> false;

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
			boolean[] secondEvaluated = {false};

			DynamicCondition alwaysFalse = ctx -> false;
			DynamicCondition trackingCondition = ctx -> {
				secondEvaluated[0] = true;
				return true;
			};

			AndCondition.AndDynamic and = new AndCondition.AndDynamic(List.of(), List.of(alwaysFalse, trackingCondition));

			assertFalse(and.test(DynamicContext.empty()));
			assertFalse(secondEvaluated[0], "AND should short-circuit on dynamic conditions");
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
			StaticCondition staticCond = ctx -> true;
			Condition condition = new Condition(List.of(staticCond), List.of());

			LogicalConditionResult result = AndCondition.from(condition);

			assertInstanceOf(AndCondition.AndStatic.class, result);
		}

		@Test
		void createsAndDynamicWhenAnyChildIsDynamic() {
			StaticCondition staticCond = ctx -> true;
			DynamicCondition dynamicCond = ctx -> true;
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
