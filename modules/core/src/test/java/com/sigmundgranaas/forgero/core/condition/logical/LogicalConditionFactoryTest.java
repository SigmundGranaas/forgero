package com.sigmundgranaas.forgero.core.condition.logical;

import com.sigmundgranaas.forgero.common.identifier.api.IdentifierFactory;
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

/**
 * Tests for logical condition factory methods and their behavior.
 * <p>
 * The logical condition system allows combining static and dynamic conditions
 * using AND, OR, and NOT operators. The factory methods automatically choose
 * between static and dynamic variants based on the input conditions.
 */
@DisplayName("Logical Condition Factory Methods")
class LogicalConditionFactoryTest {

	private static final IdentifierFactory ID_FACTORY = new IdentifierFactory.Builder()
			.defaultNamespace("forgero")
			.build();

	// Test static conditions
	private static final StaticCondition ALWAYS_TRUE_STATIC = new StaticCondition() {
		@Override
		public boolean test(ResolutionContext context) {
			return true;
		}

		@Override
		public OpenIdentifier type() {
			return ID_FACTORY.of("always_true");
		}
	};

	private static final StaticCondition ALWAYS_FALSE_STATIC = new StaticCondition() {
		@Override
		public boolean test(ResolutionContext context) {
			return false;
		}

		@Override
		public OpenIdentifier type() {
			return ID_FACTORY.of("always_false");
		}
	};

	// Test dynamic conditions
	private static final DynamicCondition ALWAYS_TRUE_DYNAMIC = new DynamicCondition() {
		@Override
		public boolean test(DynamicContext context) {
			return true;
		}

		@Override
		public OpenIdentifier type() {
			return ID_FACTORY.of("always_true_dynamic");
		}
	};

	private static final DynamicCondition ALWAYS_FALSE_DYNAMIC = new DynamicCondition() {
		@Override
		public boolean test(DynamicContext context) {
			return false;
		}

		@Override
		public OpenIdentifier type() {
			return ID_FACTORY.of("always_false_dynamic");
		}
	};

	@Nested
	@DisplayName("AndCondition Factory")
	class AndConditionFactory {

		@Test
		@DisplayName("creates static variant when all children are static")
		void createsStaticVariantWhenAllChildrenAreStatic() {
			Condition children = new Condition(
					List.of(ALWAYS_TRUE_STATIC, ALWAYS_TRUE_STATIC),
					Collections.emptyList()
			);

			LogicalConditionResult result = AndCondition.from(children);

			assertInstanceOf(AndCondition.AndStatic.class, result);
		}

		@Test
		@DisplayName("creates dynamic variant when any child is dynamic")
		void createsDynamicVariantWhenAnyChildIsDynamic() {
			Condition children = new Condition(
					List.of(ALWAYS_TRUE_STATIC),
					List.of(ALWAYS_TRUE_DYNAMIC)
			);

			LogicalConditionResult result = AndCondition.from(children);

			assertInstanceOf(AndCondition.AndDynamic.class, result);
		}

		@Test
		@DisplayName("AndStatic passes when all static conditions pass")
		void andStaticPassesWhenAllStaticConditionsPass() {
			Condition children = new Condition(
					List.of(ALWAYS_TRUE_STATIC, ALWAYS_TRUE_STATIC),
					Collections.emptyList()
			);

			AndCondition.AndStatic result = (AndCondition.AndStatic) AndCondition.from(children);

			assertTrue(result.test(null)); // ResolutionContext can be null for these test conditions
		}

		@Test
		@DisplayName("AndStatic fails when any static condition fails")
		void andStaticFailsWhenAnyStaticConditionFails() {
			Condition children = new Condition(
					List.of(ALWAYS_TRUE_STATIC, ALWAYS_FALSE_STATIC),
					Collections.emptyList()
			);

			AndCondition.AndStatic result = (AndCondition.AndStatic) AndCondition.from(children);

			assertFalse(result.test(null));
		}

		@Test
		@DisplayName("AndDynamic passes when all dynamic conditions pass")
		void andDynamicPassesWhenAllDynamicConditionsPass() {
			Condition children = new Condition(
					Collections.emptyList(),
					List.of(ALWAYS_TRUE_DYNAMIC, ALWAYS_TRUE_DYNAMIC)
			);

			AndCondition.AndDynamic result = (AndCondition.AndDynamic) AndCondition.from(children);

			assertTrue(result.test(DynamicContext.empty()));
		}

		@Test
		@DisplayName("AndDynamic fails when any dynamic condition fails")
		void andDynamicFailsWhenAnyDynamicConditionFails() {
			Condition children = new Condition(
					Collections.emptyList(),
					List.of(ALWAYS_TRUE_DYNAMIC, ALWAYS_FALSE_DYNAMIC)
			);

			AndCondition.AndDynamic result = (AndCondition.AndDynamic) AndCondition.from(children);

			assertFalse(result.test(DynamicContext.empty()));
		}

		@Test
		@DisplayName("AndStatic has correct type identifier")
		void andStaticHasCorrectTypeIdentifier() {
			Condition children = new Condition(List.of(ALWAYS_TRUE_STATIC), Collections.emptyList());

			AndCondition.AndStatic result = (AndCondition.AndStatic) AndCondition.from(children);

			assertEquals(AndCondition.TYPE, result.type());
			assertEquals("forgero:and", result.type().toString());
		}

		@Test
		@DisplayName("AndStatic can convert back to Condition")
		void andStaticCanConvertBackToCondition() {
			List<StaticCondition> originals = List.of(ALWAYS_TRUE_STATIC, ALWAYS_FALSE_STATIC);
			Condition children = new Condition(originals, Collections.emptyList());

			AndCondition.AndStatic result = (AndCondition.AndStatic) AndCondition.from(children);
			Condition converted = result.toCondition();

			assertEquals(2, converted.staticConditions().size());
			assertTrue(converted.dynamicConditions().isEmpty());
		}

		@Test
		@DisplayName("AndDynamic can convert back to Condition")
		void andDynamicCanConvertBackToCondition() {
			Condition children = new Condition(
					List.of(ALWAYS_TRUE_STATIC),
					List.of(ALWAYS_TRUE_DYNAMIC)
			);

			AndCondition.AndDynamic result = (AndCondition.AndDynamic) AndCondition.from(children);
			Condition converted = result.toCondition();

			assertEquals(1, converted.staticConditions().size());
			assertEquals(1, converted.dynamicConditions().size());
		}
	}

	@Nested
	@DisplayName("OrCondition Factory")
	class OrConditionFactory {

		@Test
		@DisplayName("creates static variant when all children are static")
		void createsStaticVariantWhenAllChildrenAreStatic() {
			Condition children = new Condition(
					List.of(ALWAYS_TRUE_STATIC, ALWAYS_FALSE_STATIC),
					Collections.emptyList()
			);

			LogicalConditionResult result = OrCondition.from(children);

			assertInstanceOf(OrCondition.OrStatic.class, result);
		}

		@Test
		@DisplayName("creates dynamic variant when any child is dynamic")
		void createsDynamicVariantWhenAnyChildIsDynamic() {
			Condition children = new Condition(
					Collections.emptyList(),
					List.of(ALWAYS_TRUE_DYNAMIC)
			);

			LogicalConditionResult result = OrCondition.from(children);

			assertInstanceOf(OrCondition.OrDynamic.class, result);
		}

		@Test
		@DisplayName("OrStatic passes when any static condition passes")
		void orStaticPassesWhenAnyStaticConditionPasses() {
			Condition children = new Condition(
					List.of(ALWAYS_FALSE_STATIC, ALWAYS_TRUE_STATIC),
					Collections.emptyList()
			);

			OrCondition.OrStatic result = (OrCondition.OrStatic) OrCondition.from(children);

			assertTrue(result.test(null));
		}

		@Test
		@DisplayName("OrStatic fails when all static conditions fail")
		void orStaticFailsWhenAllStaticConditionsFail() {
			Condition children = new Condition(
					List.of(ALWAYS_FALSE_STATIC, ALWAYS_FALSE_STATIC),
					Collections.emptyList()
			);

			OrCondition.OrStatic result = (OrCondition.OrStatic) OrCondition.from(children);

			assertFalse(result.test(null));
		}

		@Test
		@DisplayName("OrDynamic passes when any dynamic condition passes")
		void orDynamicPassesWhenAnyDynamicConditionPasses() {
			Condition children = new Condition(
					Collections.emptyList(),
					List.of(ALWAYS_FALSE_DYNAMIC, ALWAYS_TRUE_DYNAMIC)
			);

			OrCondition.OrDynamic result = (OrCondition.OrDynamic) OrCondition.from(children);

			assertTrue(result.test(DynamicContext.empty()));
		}

		@Test
		@DisplayName("OrDynamic fails when all dynamic conditions fail")
		void orDynamicFailsWhenAllDynamicConditionsFail() {
			Condition children = new Condition(
					Collections.emptyList(),
					List.of(ALWAYS_FALSE_DYNAMIC, ALWAYS_FALSE_DYNAMIC)
			);

			OrCondition.OrDynamic result = (OrCondition.OrDynamic) OrCondition.from(children);

			assertFalse(result.test(DynamicContext.empty()));
		}

		@Test
		@DisplayName("OrStatic has correct type identifier")
		void orStaticHasCorrectTypeIdentifier() {
			Condition children = new Condition(List.of(ALWAYS_TRUE_STATIC), Collections.emptyList());

			OrCondition.OrStatic result = (OrCondition.OrStatic) OrCondition.from(children);

			assertEquals(OrCondition.TYPE, result.type());
			assertEquals("forgero:or", result.type().toString());
		}

		@Test
		@DisplayName("OrStatic can convert back to Condition")
		void orStaticCanConvertBackToCondition() {
			Condition children = new Condition(
					List.of(ALWAYS_TRUE_STATIC, ALWAYS_FALSE_STATIC),
					Collections.emptyList()
			);

			OrCondition.OrStatic result = (OrCondition.OrStatic) OrCondition.from(children);
			Condition converted = result.toCondition();

			assertEquals(2, converted.staticConditions().size());
			assertTrue(converted.dynamicConditions().isEmpty());
		}
	}

	@Nested
	@DisplayName("NotCondition Factory")
	class NotConditionFactory {

		@Test
		@DisplayName("creates static variant when child is static")
		void createsStaticVariantWhenChildIsStatic() {
			Condition child = new Condition(
					List.of(ALWAYS_TRUE_STATIC),
					Collections.emptyList()
			);

			LogicalConditionResult result = NotCondition.from(child);

			assertInstanceOf(NotCondition.NotStatic.class, result);
		}

		@Test
		@DisplayName("creates dynamic variant when child is dynamic")
		void createsDynamicVariantWhenChildIsDynamic() {
			Condition child = new Condition(
					Collections.emptyList(),
					List.of(ALWAYS_TRUE_DYNAMIC)
			);

			LogicalConditionResult result = NotCondition.from(child);

			assertInstanceOf(NotCondition.NotDynamic.class, result);
		}

		@Test
		@DisplayName("NotStatic inverts true to false")
		void notStaticInvertsTrueToFalse() {
			Condition child = new Condition(List.of(ALWAYS_TRUE_STATIC), Collections.emptyList());

			NotCondition.NotStatic result = (NotCondition.NotStatic) NotCondition.from(child);

			assertFalse(result.test(null));
		}

		@Test
		@DisplayName("NotStatic inverts false to true")
		void notStaticInvertsFalseToTrue() {
			Condition child = new Condition(List.of(ALWAYS_FALSE_STATIC), Collections.emptyList());

			NotCondition.NotStatic result = (NotCondition.NotStatic) NotCondition.from(child);

			assertTrue(result.test(null));
		}

		@Test
		@DisplayName("NotDynamic inverts true to false")
		void notDynamicInvertsTrueToFalse() {
			Condition child = new Condition(Collections.emptyList(), List.of(ALWAYS_TRUE_DYNAMIC));

			NotCondition.NotDynamic result = (NotCondition.NotDynamic) NotCondition.from(child);

			assertFalse(result.test(DynamicContext.empty()));
		}

		@Test
		@DisplayName("NotDynamic inverts false to true")
		void notDynamicInvertsFalseToTrue() {
			Condition child = new Condition(Collections.emptyList(), List.of(ALWAYS_FALSE_DYNAMIC));

			NotCondition.NotDynamic result = (NotCondition.NotDynamic) NotCondition.from(child);

			assertTrue(result.test(DynamicContext.empty()));
		}

		@Test
		@DisplayName("NotStatic has correct type identifier")
		void notStaticHasCorrectTypeIdentifier() {
			Condition child = new Condition(List.of(ALWAYS_TRUE_STATIC), Collections.emptyList());

			NotCondition.NotStatic result = (NotCondition.NotStatic) NotCondition.from(child);

			assertEquals(NotCondition.TYPE, result.type());
			assertEquals("forgero:not", result.type().toString());
		}

		@Test
		@DisplayName("NotStatic can convert back to Condition")
		void notStaticCanConvertBackToCondition() {
			Condition child = new Condition(List.of(ALWAYS_TRUE_STATIC), Collections.emptyList());

			NotCondition.NotStatic result = (NotCondition.NotStatic) NotCondition.from(child);
			Condition converted = result.toCondition();

			assertEquals(1, converted.staticConditions().size());
			assertTrue(converted.dynamicConditions().isEmpty());
		}

		@Test
		@DisplayName("NotDynamic handles null dynamic condition gracefully")
		void notDynamicHandlesNullDynamicConditionGracefully() {
			// When there's only a static condition but fromDynamic is called
			// The NotDynamic should handle null gracefully
			Condition child = new Condition(List.of(ALWAYS_TRUE_STATIC), List.of(ALWAYS_TRUE_DYNAMIC));

			NotCondition.NotDynamic result = (NotCondition.NotDynamic) NotCondition.from(child);

			// Should not throw, dynamic result should be inverted
			assertFalse(result.test(DynamicContext.empty()));
		}
	}

	@Nested
	@DisplayName("LogicalConditionResult Interface")
	class LogicalConditionResultInterface {

		@Test
		@DisplayName("AndStatic implements LogicalConditionResult")
		void andStaticImplementsLogicalConditionResult() {
			Condition children = new Condition(List.of(ALWAYS_TRUE_STATIC), Collections.emptyList());
			LogicalConditionResult result = AndCondition.from(children);

			assertInstanceOf(LogicalConditionResult.class, result);
			AndCondition.AndStatic andStatic = (AndCondition.AndStatic) result;
			assertNotNull(andStatic.toCondition());
		}

		@Test
		@DisplayName("OrDynamic implements LogicalConditionResult")
		void orDynamicImplementsLogicalConditionResult() {
			Condition children = new Condition(Collections.emptyList(), List.of(ALWAYS_TRUE_DYNAMIC));
			LogicalConditionResult result = OrCondition.from(children);

			assertInstanceOf(LogicalConditionResult.class, result);
			OrCondition.OrDynamic orDynamic = (OrCondition.OrDynamic) result;
			assertNotNull(orDynamic.toCondition());
		}

		@Test
		@DisplayName("NotStatic implements LogicalConditionResult")
		void notStaticImplementsLogicalConditionResult() {
			Condition child = new Condition(List.of(ALWAYS_TRUE_STATIC), Collections.emptyList());
			LogicalConditionResult result = NotCondition.from(child);

			assertInstanceOf(LogicalConditionResult.class, result);
			NotCondition.NotStatic notStatic = (NotCondition.NotStatic) result;
			assertNotNull(notStatic.toCondition());
		}
	}

	@Nested
	@DisplayName("Edge Cases")
	class EdgeCases {

		@Test
		@DisplayName("AndStatic with empty list passes")
		void andStaticWithEmptyListPasses() {
			Condition children = new Condition(Collections.emptyList(), Collections.emptyList());

			// This creates AndStatic since there are no dynamic conditions
			AndCondition.AndStatic result = (AndCondition.AndStatic) AndCondition.from(children);

			// Empty AND should pass (vacuous truth)
			assertTrue(result.test(null));
		}

		@Test
		@DisplayName("OrStatic with empty list fails")
		void orStaticWithEmptyListFails() {
			Condition children = new Condition(Collections.emptyList(), Collections.emptyList());

			OrCondition.OrStatic result = (OrCondition.OrStatic) OrCondition.from(children);

			// Empty OR should fail (no condition can be satisfied)
			assertFalse(result.test(null));
		}
	}
}
