package com.sigmundgranaas.forgero.core.attribute.computation;

import com.sigmundgranaas.forgero.core.attribute.api.Attribute;
import com.sigmundgranaas.forgero.core.attribute.api.DefaultAttributes;
import com.sigmundgranaas.forgero.core.attribute.api.SimpleAttribute;
import com.sigmundgranaas.forgero.core.attribute.api.operator.AdditionOperator;
import com.sigmundgranaas.forgero.core.attribute.api.operator.MultiplicationOperator;
import com.sigmundgranaas.forgero.core.attribute.api.operator.Operator;
import com.sigmundgranaas.forgero.core.attribute.impl.computation.ComputationChain;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ComputationChain Behavior")
class ComputationChainBehaviorTest {

	private static Attribute attr(float value, Operator operator, int group) {
		return new SimpleAttribute(DefaultAttributes.ATTACK_DAMAGE, value, operator, group);
	}

	private static Attribute addAttr(float value) {
		return attr(value, AdditionOperator.getInstance(), 0);
	}

	private static Attribute mulAttr(float value) {
		return attr(value, MultiplicationOperator.getInstance(), 0);
	}

	@Nested
	@DisplayName("Basic Computation")
	class BasicComputation {

		@Test
		@DisplayName("computes single addition attribute")
		void computesSingleAdditionAttribute() {
			ComputationChain chain = new ComputationChain(List.of(addAttr(5f)));

			float result = chain.compute(0f);

			assertEquals(5f, result, 0.001f);
		}

		@Test
		@DisplayName("computes single multiplication attribute")
		void computesSingleMultiplicationAttribute() {
			ComputationChain chain = new ComputationChain(List.of(mulAttr(2f)));

			float result = chain.compute(10f);

			assertEquals(20f, result, 0.001f);
		}

		@Test
		@DisplayName("sums multiple addition attributes")
		void sumsMultipleAdditionAttributes() {
			ComputationChain chain = new ComputationChain(List.of(
					addAttr(5f),
					addAttr(3f),
					addAttr(2f)
			));

			float result = chain.compute(0f);

			assertEquals(10f, result, 0.001f);
		}

		@Test
		@DisplayName("chains multiple multiplication attributes")
		void chainsMultipleMultiplicationAttributes() {
			ComputationChain chain = new ComputationChain(List.of(
					mulAttr(2f),
					mulAttr(3f)
			));

			float result = chain.compute(1f);

			assertEquals(6f, result, 0.001f);
		}

		@Test
		@DisplayName("empty chain returns base value")
		void emptyChainReturnsBaseValue() {
			ComputationChain chain = new ComputationChain(List.of());

			float result = chain.compute(42f);

			assertEquals(42f, result, 0.001f);
		}
	}

	@Nested
	@DisplayName("Operator Precedence")
	class OperatorPrecedence {

		@Test
		@DisplayName("addition applied before multiplication")
		void additionAppliedBeforeMultiplication() {
			ComputationChain chain = new ComputationChain(List.of(
					addAttr(10f),
					mulAttr(2f)
			));

			float result = chain.compute(0f);

			assertEquals(20f, result, 0.001f);
		}

		@Test
		@DisplayName("unsorted input is sorted by operator order")
		void unsortedInputIsSortedByOperatorOrder() {
			ComputationChain chain = new ComputationChain(List.of(
					mulAttr(2f),
					addAttr(10f)
			));

			float result = chain.compute(0f);

			assertEquals(20f, result, 0.001f);
		}

		@Test
		@DisplayName("complex chain respects operator precedence")
		void complexChainRespectsOperatorPrecedence() {
			ComputationChain chain = new ComputationChain(List.of(
					addAttr(5f),
					mulAttr(1.5f),
					addAttr(10f),
					mulAttr(2f)
			));

			float result = chain.compute(0f);

			assertEquals(45f, result, 0.001f);
		}
	}

	@Nested
	@DisplayName("Group Ordering")
	class GroupOrdering {

		@Test
		@DisplayName("attributes in same group computed together")
		void attributesInSameGroupComputedTogether() {
			ComputationChain chain = new ComputationChain(List.of(
					attr(10f, AdditionOperator.getInstance(), 0),
					attr(2f, MultiplicationOperator.getInstance(), 0)
			));

			float result = chain.compute(0f);

			assertEquals(20f, result, 0.001f);
		}

		@Test
		@DisplayName("lower group computed before higher group")
		void lowerGroupComputedBeforeHigherGroup() {
			ComputationChain chain = new ComputationChain(List.of(
					attr(10f, AdditionOperator.getInstance(), 0),
					attr(5f, AdditionOperator.getInstance(), 1)
			));

			assertEquals(15f, chain.compute(0f), 0.001f);
		}

		@Test
		@DisplayName("groups maintain operator precedence within each group")
		void groupsMaintainOperatorPrecedenceWithinEachGroup() {
			ComputationChain chain = new ComputationChain(List.of(
					attr(10f, AdditionOperator.getInstance(), 0),
					attr(2f, MultiplicationOperator.getInstance(), 0),
					attr(5f, AdditionOperator.getInstance(), 1),
					attr(1.5f, MultiplicationOperator.getInstance(), 1)
			));

			float result = chain.compute(0f);

			assertEquals(37.5f, result, 0.001f);
		}

		@Test
		@DisplayName("unsorted groups are sorted correctly")
		void unsortedGroupsAreSortedCorrectly() {
			ComputationChain chain = new ComputationChain(List.of(
					attr(5f, AdditionOperator.getInstance(), 1),
					attr(10f, AdditionOperator.getInstance(), 0)
			));

			assertEquals(15f, chain.compute(0f), 0.001f);
		}
	}

	@Nested
	@DisplayName("Builder Pattern")
	class BuilderPattern {

		@Test
		@DisplayName("builder creates chain with attributes")
		void builderCreatesChainWithAttributes() {
			ComputationChain chain = new ComputationChain.Builder()
					.addAttribute(addAttr(5f))
					.addAttribute(addAttr(3f))
					.build();

			assertEquals(8f, chain.compute(0f), 0.001f);
		}

		@Test
		@DisplayName("builder can add attributes incrementally")
		void builderCanAddAttributesIncrementally() {
			ComputationChain.Builder builder = new ComputationChain.Builder();
			builder.addAttribute(addAttr(1f));
			builder.addAttribute(addAttr(2f));
			builder.addAttribute(addAttr(3f));

			ComputationChain chain = builder.build();

			assertEquals(6f, chain.compute(0f), 0.001f);
		}

		@Test
		@DisplayName("builder produces sorted chain")
		void builderProducesSortedChain() {
			ComputationChain chain = new ComputationChain.Builder()
					.addAttribute(mulAttr(2f))
					.addAttribute(addAttr(10f))
					.build();

			assertEquals(20f, chain.compute(0f), 0.001f);
		}
	}

	@Nested
	@DisplayName("Edge Cases")
	class EdgeCases {

		@Test
		@DisplayName("handles zero multiplication")
		void handlesZeroMultiplication() {
			ComputationChain chain = new ComputationChain(List.of(
					addAttr(100f),
					mulAttr(0f)
			));

			assertEquals(0f, chain.compute(0f), 0.001f);
		}

		@Test
		@DisplayName("handles negative values")
		void handlesNegativeValues() {
			ComputationChain chain = new ComputationChain(List.of(
					addAttr(-5f),
					addAttr(10f)
			));

			assertEquals(5f, chain.compute(0f), 0.001f);
		}

		@Test
		@DisplayName("handles fractional values")
		void handlesFractionalValues() {
			ComputationChain chain = new ComputationChain(List.of(
					addAttr(1.5f),
					mulAttr(0.5f)
			));

			assertEquals(0.75f, chain.compute(0f), 0.001f);
		}

		@Test
		@DisplayName("handles large values")
		void handlesLargeValues() {
			ComputationChain chain = new ComputationChain(List.of(
					addAttr(1000000f),
					mulAttr(1000f)
			));

			float result = chain.compute(0f);
			assertEquals(1_000_000_000f, result, 0.001f);
		}
	}
}
