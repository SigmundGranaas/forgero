package com.sigmundgranaas.forgero.common.runtime;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.condition.api.Condition;
import com.sigmundgranaas.forgero.core.condition.api.DynamicCondition;
import com.sigmundgranaas.forgero.core.condition.logical.AndCondition;
import com.sigmundgranaas.forgero.core.condition.logical.NotCondition;
import com.sigmundgranaas.forgero.core.condition.logical.OrCondition;
import com.sigmundgranaas.forgero.core.property.api.custom.ConditionalProperty;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the game-side dynamic condition evaluator.
 *
 * <p>These tests cover the evaluation semantics that used to live core-side on
 * DynamicCondition/ConditionalProperty (ported here when core became a pure
 * compile-time layer): AND requires all children, OR requires any, NOT inverts,
 * the Condition.any() wrapper passes if any inner Condition's dynamic conditions
 * all pass (static-only inner conditions count as a pass), and property filtering
 * keeps properties whose dynamic conditions pass.
 */
@DisplayName("RuntimeConditions")
class RuntimeConditionsTest {

	private static final Key<Boolean> IS_SNEAKING = new Key<>(new OpenIdentifier("test", "is_sneaking"));

	/**
	 * Game-side evaluable stub with a fixed result.
	 */
	private record FixedCondition(boolean result) implements EvaluableCondition {
		@Override
		public boolean test(DynamicContext context) {
			return result;
		}

		@Override
		public OpenIdentifier type() {
			return new OpenIdentifier("test", "fixed");
		}
	}

	/**
	 * Game-side evaluable stub that reads runtime state from the context.
	 */
	private record SneakingCondition(boolean expected) implements EvaluableCondition {
		@Override
		public boolean test(DynamicContext context) {
			return context.get(IS_SNEAKING).orElse(false) == expected;
		}

		@Override
		public OpenIdentifier type() {
			return new OpenIdentifier("test", "sneaking");
		}
	}

	/**
	 * A data-only dynamic condition without game-side evaluation support.
	 */
	private record OpaqueCondition() implements DynamicCondition {
		@Override
		public OpenIdentifier type() {
			return new OpenIdentifier("test", "opaque");
		}
	}

	private record TestProperty(String name, @Nullable Condition condition) implements ConditionalProperty {
	}

	private static DynamicContext sneakingContext(boolean sneaking) {
		return new DynamicContext.Builder().put(IS_SNEAKING, sneaking).build();
	}

	@Nested
	@DisplayName("test(Condition, DynamicContext)")
	class TestCondition {

		@Test
		@DisplayName("null condition passes")
		void nullConditionPasses() {
			assertTrue(RuntimeConditions.test(null, DynamicContext.empty()));
		}

		@Test
		@DisplayName("condition with no dynamic conditions passes")
		void noDynamicConditionsPasses() {
			Condition condition = Condition.ofStatic();
			assertTrue(RuntimeConditions.test(condition, DynamicContext.empty()));
		}

		@Test
		@DisplayName("passes when all dynamic conditions pass")
		void passesWhenAllDynamicConditionsPass() {
			Condition condition = Condition.ofDynamic(new FixedCondition(true), new FixedCondition(true));
			assertTrue(RuntimeConditions.test(condition, DynamicContext.empty()));
		}

		@Test
		@DisplayName("fails when any dynamic condition fails")
		void failsWhenAnyDynamicConditionFails() {
			Condition condition = Condition.ofDynamic(new FixedCondition(true), new FixedCondition(false));
			assertFalse(RuntimeConditions.test(condition, DynamicContext.empty()));
		}

		@Test
		@DisplayName("evaluates conditions against the supplied runtime context")
		void evaluatesAgainstRuntimeContext() {
			Condition condition = Condition.ofDynamic(new SneakingCondition(true));

			assertTrue(RuntimeConditions.test(condition, sneakingContext(true)));
			assertFalse(RuntimeConditions.test(condition, sneakingContext(false)));
			assertFalse(RuntimeConditions.test(condition, DynamicContext.empty()),
					"Missing context data should fail the sneaking requirement");
		}
	}

	@Nested
	@DisplayName("testCondition(DynamicCondition, DynamicContext)")
	class TestSingleCondition {

		@Test
		@DisplayName("evaluates EvaluableCondition directly")
		void evaluatesEvaluableCondition() {
			assertTrue(RuntimeConditions.testCondition(new FixedCondition(true), DynamicContext.empty()));
			assertFalse(RuntimeConditions.testCondition(new FixedCondition(false), DynamicContext.empty()));
		}

		@Test
		@DisplayName("unknown data-only condition types fail closed")
		void unknownConditionTypesFailClosed() {
			assertFalse(RuntimeConditions.testCondition(new OpaqueCondition(), DynamicContext.empty()));
		}
	}

	@Nested
	@DisplayName("Condition.any() OR wrapper semantics")
	class OrWrapperSemantics {

		private DynamicCondition orWrapper(Condition... inner) {
			Condition result = Condition.any(inner);
			assertEquals(1, result.dynamicConditions().size(), "any() should produce a single OR wrapper");
			return result.dynamicConditions().get(0);
		}

		@Test
		@DisplayName("fails when all inner dynamic conditions fail")
		void failsWhenAllInnerDynamicFail() {
			DynamicCondition or = orWrapper(
					Condition.ofDynamic(new FixedCondition(false)),
					Condition.ofDynamic(new FixedCondition(false))
			);

			assertFalse(RuntimeConditions.testCondition(or, DynamicContext.empty()));
		}

		@Test
		@DisplayName("passes when any inner dynamic condition passes")
		void passesWhenAnyInnerDynamicPasses() {
			DynamicCondition or = orWrapper(
					Condition.ofDynamic(new FixedCondition(false)),
					Condition.ofDynamic(new FixedCondition(true))
			);

			assertTrue(RuntimeConditions.testCondition(or, DynamicContext.empty()));
		}

		@Test
		@DisplayName("inner condition without dynamic conditions counts as a pass")
		void innerConditionWithoutDynamicCountsAsPass() {
			// Static conditions of the inner condition already passed during compilation.
			Condition staticOnly = Condition.ofStatic();
			DynamicCondition or = orWrapper(
					staticOnly,
					Condition.ofDynamic(new FixedCondition(false))
			);

			assertTrue(RuntimeConditions.testCondition(or, DynamicContext.empty()));
		}

		@Test
		@DisplayName("ALWAYS_TRUE inner condition counts as a pass")
		void alwaysTrueInnerConditionCountsAsPass() {
			DynamicCondition or = orWrapper(
					Condition.ALWAYS_TRUE,
					Condition.ofDynamic(new FixedCondition(false))
			);

			assertTrue(RuntimeConditions.testCondition(or, DynamicContext.empty()));
		}

		@Test
		@DisplayName("inner condition passes only if ALL of its dynamic conditions pass")
		void innerConditionRequiresAllItsDynamicConditions() {
			Condition mixed = Condition.ofDynamic(new FixedCondition(true), new FixedCondition(false));
			DynamicCondition or = orWrapper(
					mixed,
					Condition.ofDynamic(new FixedCondition(false))
			);

			assertFalse(RuntimeConditions.testCondition(or, DynamicContext.empty()));
		}
	}

	@Nested
	@DisplayName("Logical condition evaluation")
	class LogicalConditions {

		@Test
		@DisplayName("AndDynamic passes when all dynamic conditions pass")
		void andDynamicPassesWhenAllPass() {
			AndCondition.AndDynamic and = new AndCondition.AndDynamic(
					Collections.emptyList(),
					List.of(new FixedCondition(true), new FixedCondition(true))
			);

			assertTrue(RuntimeConditions.testCondition(and, DynamicContext.empty()));
		}

		@Test
		@DisplayName("AndDynamic fails when any dynamic condition fails")
		void andDynamicFailsWhenAnyFails() {
			AndCondition.AndDynamic and = new AndCondition.AndDynamic(
					Collections.emptyList(),
					List.of(new FixedCondition(true), new FixedCondition(false))
			);

			assertFalse(RuntimeConditions.testCondition(and, DynamicContext.empty()));
		}

		@Test
		@DisplayName("OrDynamic passes when any dynamic condition passes")
		void orDynamicPassesWhenAnyPasses() {
			OrCondition.OrDynamic or = new OrCondition.OrDynamic(
					Collections.emptyList(),
					List.of(new FixedCondition(false), new FixedCondition(true))
			);

			assertTrue(RuntimeConditions.testCondition(or, DynamicContext.empty()));
		}

		@Test
		@DisplayName("OrDynamic fails when all dynamic conditions fail")
		void orDynamicFailsWhenAllFail() {
			OrCondition.OrDynamic or = new OrCondition.OrDynamic(
					Collections.emptyList(),
					List.of(new FixedCondition(false), new FixedCondition(false))
			);

			assertFalse(RuntimeConditions.testCondition(or, DynamicContext.empty()));
		}

		@Test
		@DisplayName("NotDynamic inverts true to false")
		void notDynamicInvertsTrueToFalse() {
			NotCondition.NotDynamic not = new NotCondition.NotDynamic(null, new FixedCondition(true));

			assertFalse(RuntimeConditions.testCondition(not, DynamicContext.empty()));
		}

		@Test
		@DisplayName("NotDynamic inverts false to true")
		void notDynamicInvertsFalseToTrue() {
			NotCondition.NotDynamic not = new NotCondition.NotDynamic(null, new FixedCondition(false));

			assertTrue(RuntimeConditions.testCondition(not, DynamicContext.empty()));
		}

		@Test
		@DisplayName("NotDynamic with null child treats the child as passing and inverts it")
		void notDynamicWithNullChildFails() {
			NotCondition.NotDynamic not = new NotCondition.NotDynamic(null, null);

			assertFalse(RuntimeConditions.testCondition(not, DynamicContext.empty()));
		}
	}

	@Nested
	@DisplayName("filter(Collection, DynamicContext)")
	class Filter {

		@Test
		@DisplayName("keeps properties without conditions")
		void keepsPropertiesWithoutConditions() {
			TestProperty unconditional = new TestProperty("unconditional", null);

			List<TestProperty> result = RuntimeConditions.filter(List.of(unconditional), DynamicContext.empty());

			assertEquals(List.of(unconditional), result);
		}

		@Test
		@DisplayName("filters by dynamic conditions against runtime context")
		void filtersByDynamicConditions() {
			TestProperty whenSneaking = new TestProperty("sneaking", Condition.ofDynamic(new SneakingCondition(true)));
			TestProperty whenNotSneaking = new TestProperty("not_sneaking", Condition.ofDynamic(new SneakingCondition(false)));
			TestProperty always = new TestProperty("always", null);
			List<TestProperty> properties = List.of(whenSneaking, whenNotSneaking, always);

			List<TestProperty> sneaking = RuntimeConditions.filter(properties, sneakingContext(true));
			assertEquals(List.of(whenSneaking, always), sneaking);

			List<TestProperty> standing = RuntimeConditions.filter(properties, sneakingContext(false));
			assertEquals(List.of(whenNotSneaking, always), standing);
		}

		@Test
		@DisplayName("empty collection yields empty list")
		void emptyCollectionYieldsEmptyList() {
			assertTrue(RuntimeConditions.filter(List.<TestProperty>of(), DynamicContext.empty()).isEmpty());
		}
	}
}
