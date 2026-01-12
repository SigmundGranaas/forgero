package com.sigmundgranaas.forgero.core.attribute.api;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.attribute.api.operator.AdditionOperator;
import com.sigmundgranaas.forgero.core.attribute.api.operator.MultiplicationOperator;
import com.sigmundgranaas.forgero.core.condition.api.Condition;
import com.sigmundgranaas.forgero.core.condition.api.DynamicCondition;
import com.sigmundgranaas.forgero.core.property.context.DynamicContext;
import com.sigmundgranaas.forgero.core.property.context.Key;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PrecomputedAttribute")
class PrecomputedAttributeTest {

	private static final OpenIdentifier ATTACK_DAMAGE = DefaultAttributes.ATTACK_DAMAGE;
	private static final OpenIdentifier SNEAKING_KEY = OpenIdentifier.parse("test:is_sneaking");
	private static final Key<Boolean> IS_SNEAKING = new Key<>(SNEAKING_KEY);

	/**
	 * Creates a simple conditional attribute that activates when sneaking.
	 */
	private static Attribute conditionalAttr(float value, boolean activatesWhenSneaking) {
		DynamicCondition sneakCondition = new DynamicCondition() {
			@Override
			public boolean test(DynamicContext context) {
				return context.get(IS_SNEAKING).orElse(false) == activatesWhenSneaking;
			}

			@Override
			public OpenIdentifier type() {
				return SNEAKING_KEY;
			}
		};

		Condition condition = new Condition(List.of(), List.of(sneakCondition));
		return new SimpleAttribute(ATTACK_DAMAGE, value, AdditionOperator.getInstance(), 0, condition);
	}

	/**
	 * Creates an unconditional attribute (should not be in conditionalAttributes list).
	 */
	private static Attribute unconditionalAttr(float value) {
		return new SimpleAttribute(ATTACK_DAMAGE, value);
	}

	private static DynamicContext sneakingContext(boolean isSneaking) {
		return new DynamicContext.Builder()
				.put(IS_SNEAKING, isSneaking)
				.build();
	}

	@Nested
	@DisplayName("compute()")
	class Compute {

		@Test
		@DisplayName("with no conditionals returns base value immediately")
		void withNoConditionalsReturnsBaseValue() {
			PrecomputedAttribute precomputed = new PrecomputedAttribute(10.0f, List.of());

			float result = precomputed.compute(DynamicContext.empty());

			assertEquals(10.0f, result, 0.001f);
		}

		@Test
		@DisplayName("with active conditional applies it to base value")
		void withActiveConditionalAppliesIt() {
			Attribute conditional = conditionalAttr(5.0f, true);
			PrecomputedAttribute precomputed = new PrecomputedAttribute(10.0f, List.of(conditional));

			float result = precomputed.compute(sneakingContext(true));

			assertEquals(15.0f, result, 0.001f);
		}

		@Test
		@DisplayName("with inactive conditional ignores it")
		void withInactiveConditionalIgnoresIt() {
			Attribute conditional = conditionalAttr(5.0f, true);
			PrecomputedAttribute precomputed = new PrecomputedAttribute(10.0f, List.of(conditional));

			float result = precomputed.compute(sneakingContext(false));

			assertEquals(10.0f, result, 0.001f);
		}

		@Test
		@DisplayName("with mixed conditionals applies only active ones")
		void withMixedConditionalsAppliesOnlyActive() {
			Attribute activates = conditionalAttr(5.0f, true);
			Attribute doesNotActivate = conditionalAttr(100.0f, false);
			PrecomputedAttribute precomputed = new PrecomputedAttribute(
					10.0f,
					List.of(activates, doesNotActivate)
			);

			float result = precomputed.compute(sneakingContext(true));

			assertEquals(15.0f, result, 0.001f);
		}

		@Test
		@DisplayName("with multiple active conditionals applies all")
		void withMultipleActiveConditionalsAppliesAll() {
			Attribute conditional1 = conditionalAttr(5.0f, true);
			Attribute conditional2 = conditionalAttr(3.0f, true);
			PrecomputedAttribute precomputed = new PrecomputedAttribute(
					10.0f,
					List.of(conditional1, conditional2)
			);

			float result = precomputed.compute(sneakingContext(true));

			assertEquals(18.0f, result, 0.001f);
		}

		@Test
		@DisplayName("respects operator precedence in conditionals")
		void respectsOperatorPrecedenceInConditionals() {
			// Multiplication conditional: base * 2 after additions
			DynamicCondition alwaysTrue = new DynamicCondition() {
				@Override
				public boolean test(DynamicContext context) {
					return true;
				}

				@Override
				public OpenIdentifier type() {
					return OpenIdentifier.parse("test:always_true");
				}
			};

			Condition condition = new Condition(List.of(), List.of(alwaysTrue));
			Attribute mulConditional = new SimpleAttribute(
					ATTACK_DAMAGE, 2.0f, MultiplicationOperator.getInstance(), 0, condition
			);

			PrecomputedAttribute precomputed = new PrecomputedAttribute(10.0f, List.of(mulConditional));

			float result = precomputed.compute(DynamicContext.empty());

			assertEquals(20.0f, result, 0.001f);
		}
	}

	@Nested
	@DisplayName("isFullyPrecomputed()")
	class IsFullyPrecomputed {

		@Test
		@DisplayName("returns true when no conditional attributes")
		void returnsTrueWhenNoConditionalAttributes() {
			PrecomputedAttribute precomputed = new PrecomputedAttribute(10.0f, List.of());

			assertTrue(precomputed.isFullyPrecomputed());
		}

		@Test
		@DisplayName("returns false when conditional attributes exist")
		void returnsFalseWhenConditionalAttributesExist() {
			Attribute conditional = conditionalAttr(5.0f, true);
			PrecomputedAttribute precomputed = new PrecomputedAttribute(10.0f, List.of(conditional));

			assertFalse(precomputed.isFullyPrecomputed());
		}
	}

	@Nested
	@DisplayName("ZERO constant")
	class ZeroConstant {

		@Test
		@DisplayName("has zero base value")
		void hasZeroBaseValue() {
			assertEquals(0f, PrecomputedAttribute.ZERO.baseValue(), 0.001f);
		}

		@Test
		@DisplayName("has empty conditional list")
		void hasEmptyConditionalList() {
			assertTrue(PrecomputedAttribute.ZERO.conditionalAttributes().isEmpty());
		}

		@Test
		@DisplayName("is fully precomputed")
		void isFullyPrecomputed() {
			assertTrue(PrecomputedAttribute.ZERO.isFullyPrecomputed());
		}

		@Test
		@DisplayName("compute returns zero")
		void computeReturnsZero() {
			assertEquals(0f, PrecomputedAttribute.ZERO.compute(DynamicContext.empty()), 0.001f);
		}
	}

	@Nested
	@DisplayName("Immutability")
	class Immutability {

		@Test
		@DisplayName("conditional list is defensively copied")
		void conditionalListIsDefensivelyCopied() {
			Attribute conditional = conditionalAttr(5.0f, true);
			List<Attribute> mutableList = new java.util.ArrayList<>();
			mutableList.add(conditional);

			PrecomputedAttribute precomputed = new PrecomputedAttribute(10.0f, mutableList);

			// Modify original list
			mutableList.clear();

			// PrecomputedAttribute should still have the conditional
			assertFalse(precomputed.conditionalAttributes().isEmpty());
		}

		@Test
		@DisplayName("returned conditional list is immutable")
		void returnedConditionalListIsImmutable() {
			Attribute conditional = conditionalAttr(5.0f, true);
			PrecomputedAttribute precomputed = new PrecomputedAttribute(10.0f, List.of(conditional));

			assertThrows(UnsupportedOperationException.class, () ->
					precomputed.conditionalAttributes().clear()
			);
		}
	}
}
