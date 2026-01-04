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

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NotConditionTest {

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

	@Nested
	@DisplayName("NotStatic")
	class NotStaticTests {

		@Test
		void invertsTrue() {
			StaticCondition alwaysTrue = new TestStaticCondition(true);

			NotCondition.NotStatic not = new NotCondition.NotStatic(alwaysTrue);

			assertFalse(not.test(null));
		}

		@Test
		void invertsFalse() {
			StaticCondition alwaysFalse = new TestStaticCondition(false);

			NotCondition.NotStatic not = new NotCondition.NotStatic(alwaysFalse);

			assertTrue(not.test(null));
		}

		@Test
		void hasCorrectType() {
			StaticCondition condition = new TestStaticCondition(true);

			NotCondition.NotStatic not = new NotCondition.NotStatic(condition);

			assertEquals(NotCondition.TYPE, not.type());
		}

		@Test
		void toConditionPreservesStructure() {
			StaticCondition condition = new TestStaticCondition(true);
			NotCondition.NotStatic not = new NotCondition.NotStatic(condition);

			Condition result = not.toCondition();

			assertEquals(1, result.staticConditions().size());
			assertEquals(condition, result.staticConditions().get(0));
			assertTrue(result.dynamicConditions().isEmpty());
		}
	}

	@Nested
	@DisplayName("NotDynamic")
	class NotDynamicTests {

		@Test
		void invertsDynamicTrue() {
			DynamicCondition alwaysTrue = new TestDynamicCondition(true);

			NotCondition.NotDynamic not = new NotCondition.NotDynamic(null, alwaysTrue);

			assertFalse(not.test(DynamicContext.empty()));
		}

		@Test
		void invertsDynamicFalse() {
			DynamicCondition alwaysFalse = new TestDynamicCondition(false);

			NotCondition.NotDynamic not = new NotCondition.NotDynamic(null, alwaysFalse);

			assertTrue(not.test(DynamicContext.empty()));
		}

		@Test
		void handlesNullDynamicCondition() {
			// According to the implementation, null dynamicCond is treated as true, then inverted to false
			NotCondition.NotDynamic not = new NotCondition.NotDynamic(null, null);

			assertFalse(not.test(DynamicContext.empty()));
		}

		@Test
		void hasCorrectType() {
			DynamicCondition condition = new TestDynamicCondition(true);

			NotCondition.NotDynamic not = new NotCondition.NotDynamic(null, condition);

			assertEquals(NotCondition.TYPE, not.type());
		}

		@Test
		void toConditionPreservesStructure() {
			DynamicCondition dynamicCond = new TestDynamicCondition(true);
			NotCondition.NotDynamic not = new NotCondition.NotDynamic(null, dynamicCond);

			Condition result = not.toCondition();

			assertTrue(result.staticConditions().isEmpty());
			assertEquals(1, result.dynamicConditions().size());
			assertEquals(dynamicCond, result.dynamicConditions().get(0));
		}
	}

	@Nested
	@DisplayName("Factory Method")
	class FactoryTests {

		@Test
		void createsNotStaticWhenChildIsStatic() {
			StaticCondition staticCond = new TestStaticCondition(true);
			Condition condition = new Condition(List.of(staticCond), Collections.emptyList());

			LogicalConditionResult result = NotCondition.from(condition);

			assertInstanceOf(NotCondition.NotStatic.class, result);
		}

		@Test
		void createsNotDynamicWhenChildIsDynamic() {
			DynamicCondition dynamicCond = new TestDynamicCondition(true);
			Condition condition = new Condition(Collections.emptyList(), List.of(dynamicCond));

			LogicalConditionResult result = NotCondition.from(condition);

			assertInstanceOf(NotCondition.NotDynamic.class, result);
		}

		@Test
		void createsNotDynamicWhenChildHasBoth() {
			StaticCondition staticCond = new TestStaticCondition(true);
			DynamicCondition dynamicCond = new TestDynamicCondition(true);
			Condition condition = new Condition(List.of(staticCond), List.of(dynamicCond));

			LogicalConditionResult result = NotCondition.from(condition);

			assertInstanceOf(NotCondition.NotDynamic.class, result);
		}

		@Test
		void notStaticInvertsCorrectly() {
			StaticCondition alwaysTrue = new TestStaticCondition(true);
			Condition condition = new Condition(List.of(alwaysTrue), Collections.emptyList());

			NotCondition.NotStatic not = (NotCondition.NotStatic) NotCondition.from(condition);

			assertFalse(not.test(null));
		}

		@Test
		void notDynamicInvertsCorrectly() {
			DynamicCondition alwaysFalse = new TestDynamicCondition(false);
			Condition condition = new Condition(Collections.emptyList(), List.of(alwaysFalse));

			NotCondition.NotDynamic not = (NotCondition.NotDynamic) NotCondition.from(condition);

			assertTrue(not.test(DynamicContext.empty()));
		}
	}
}
