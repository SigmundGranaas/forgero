package com.sigmundgranaas.forgero.core.attribute.api;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.attribute.api.operator.AdditionOperator;
import com.sigmundgranaas.forgero.core.attribute.api.operator.MultiplicationOperator;
import com.sigmundgranaas.forgero.core.condition.api.Condition;
import com.sigmundgranaas.forgero.core.condition.api.DynamicCondition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PrecomputedAttribute")
class PrecomputedAttributeTest {

	private static final OpenIdentifier ATTACK_DAMAGE = DefaultAttributes.ATTACK_DAMAGE;
	private static final OpenIdentifier SNEAKING_KEY = OpenIdentifier.parse("test:is_sneaking");

	/**
	 * Creates a conditional attribute carrying an opaque dynamic condition.
	 * Core never evaluates the condition - it is data for the game layer.
	 */
	private static Attribute conditionalAttr(float value) {
		DynamicCondition sneakCondition = () -> SNEAKING_KEY;

		Condition condition = new Condition(List.of(), List.of(sneakCondition));
		return new SimpleAttribute(ATTACK_DAMAGE, value, AdditionOperator.getInstance(), 0, condition);
	}

	/**
	 * Creates an unconditional attribute (should not be in conditionalAttributes list).
	 */
	private static Attribute unconditionalAttr(float value) {
		return new SimpleAttribute(ATTACK_DAMAGE, value);
	}

	@Nested
	@DisplayName("value()")
	class Value {

		@Test
		@DisplayName("with no conditionals returns base value")
		void withNoConditionalsReturnsBaseValue() {
			PrecomputedAttribute precomputed = new PrecomputedAttribute(10.0f, List.of());

			float result = precomputed.value();

			assertEquals(10.0f, result, 0.001f);
		}

		@Test
		@DisplayName("dynamic-conditional attributes do not contribute to the compiled value")
		void conditionalAttributesDoNotContribute() {
			Attribute conditional = conditionalAttr(5.0f);
			PrecomputedAttribute precomputed = new PrecomputedAttribute(10.0f, List.of(conditional));

			float result = precomputed.value();

			assertEquals(10.0f, result, 0.001f);
		}

		@Test
		@DisplayName("multiple conditionals still do not contribute to the compiled value")
		void multipleConditionalsDoNotContribute() {
			Attribute conditional1 = conditionalAttr(5.0f);
			Attribute conditional2 = conditionalAttr(3.0f);
			PrecomputedAttribute precomputed = new PrecomputedAttribute(
					10.0f,
					List.of(conditional1, conditional2)
			);

			float result = precomputed.value();

			assertEquals(10.0f, result, 0.001f);
		}

		@Test
		@DisplayName("multiplicative conditionals are carried as data, not applied")
		void multiplicativeConditionalsAreNotApplied() {
			DynamicCondition alwaysTrue = () -> OpenIdentifier.parse("test:always_true");

			Condition condition = new Condition(List.of(), List.of(alwaysTrue));
			Attribute mulConditional = new SimpleAttribute(
					ATTACK_DAMAGE, 2.0f, MultiplicationOperator.getInstance(), 0, condition
			);

			PrecomputedAttribute precomputed = new PrecomputedAttribute(10.0f, List.of(mulConditional));

			float result = precomputed.value();

			assertEquals(10.0f, result, 0.001f);
		}
	}

	@Nested
	@DisplayName("conditionalAttributes()")
	class ConditionalAttributes {

		@Test
		@DisplayName("carries dynamic-conditional attributes as data for the game layer")
		void carriesConditionalAttributesAsData() {
			Attribute conditional = conditionalAttr(5.0f);
			PrecomputedAttribute precomputed = new PrecomputedAttribute(10.0f, List.of(conditional));

			assertEquals(1, precomputed.conditionalAttributes().size());
			assertSame(conditional, precomputed.conditionalAttributes().get(0));
		}

		@Test
		@DisplayName("carries all conditionals in order")
		void carriesAllConditionalsInOrder() {
			Attribute conditional1 = conditionalAttr(5.0f);
			Attribute conditional2 = conditionalAttr(3.0f);
			PrecomputedAttribute precomputed = new PrecomputedAttribute(
					10.0f,
					List.of(conditional1, conditional2)
			);

			assertEquals(List.of(conditional1, conditional2), precomputed.conditionalAttributes());
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
			Attribute conditional = conditionalAttr(5.0f);
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
		@DisplayName("value returns zero")
		void valueReturnsZero() {
			assertEquals(0f, PrecomputedAttribute.ZERO.value(), 0.001f);
		}
	}

	@Nested
	@DisplayName("Immutability")
	class Immutability {

		@Test
		@DisplayName("conditional list is defensively copied")
		void conditionalListIsDefensivelyCopied() {
			Attribute conditional = conditionalAttr(5.0f);
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
			Attribute conditional = conditionalAttr(5.0f);
			PrecomputedAttribute precomputed = new PrecomputedAttribute(10.0f, List.of(conditional));

			assertThrows(UnsupportedOperationException.class, () ->
					precomputed.conditionalAttributes().clear()
			);
		}
	}
}
