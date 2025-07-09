package com.sigmundgranaas.forgero.core.property.engine;

import com.sigmundgranaas.forgero.core.attribute.api.CompositeAttribute;
import com.sigmundgranaas.forgero.core.attribute.api.CompositeAttributeComponent;
import com.sigmundgranaas.forgero.core.attribute.api.SimpleAttribute;
import com.sigmundgranaas.forgero.core.attribute.api.DefaultAttributes;
import com.sigmundgranaas.forgero.core.attribute.impl.computation.CalculationVisualizer;
import com.sigmundgranaas.forgero.core.attribute.impl.computation.ComputationChain;
import com.sigmundgranaas.forgero.core.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.property.condition.Condition;
import com.sigmundgranaas.forgero.core.attribute.api.operator.AdditionOperator;
import com.sigmundgranaas.forgero.core.attribute.api.operator.MultiplicationOperator;
import com.sigmundgranaas.forgero.core.attribute.api.operator.DivisionOperator;
import com.sigmundgranaas.forgero.core.attribute.api.operator.SubtractionOperator;
import com.sigmundgranaas.forgero.core.attribute.api.operator.MinOperator;
import com.sigmundgranaas.forgero.core.attribute.api.operator.MaxOperator;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ComputationChainTest {
	@Test
	void computesCorrectValueWithSimpleAddition() {
		var attributes = List.of(
				new SimpleAttribute(DefaultAttributes.ATTACK_DAMAGE, 5f), // Default is AdditionOperator, group 0
				new SimpleAttribute(DefaultAttributes.ATTACK_DAMAGE, 3f) // Default is AdditionOperator, group 0
		);
		var chain = new ComputationChain(attributes);
		// Sorted: [Add 3f, Add 5f] (or vice versa, order 1 for both)
		// Calculation: 0 + 3 + 5 = 8 (or 0 + 5 + 3 = 8)
		assertEquals(8f, chain.compute(0f));
	}

	@Test
	void computesMixedOperatorsAndGroupsBasicExample() {
		var attributes = List.of(
				// Group 0: (Initial 0 + 5 - 1) * 1.2 = 4 * 1.2 = 4.8
				new SimpleAttribute(DefaultAttributes.ATTACK_DAMAGE, 5f, AdditionOperator.getInstance(), 0, Condition.ALWAYS_TRUE),       // Add 5 (Order 1)
				new SimpleAttribute(DefaultAttributes.ATTACK_DAMAGE, 1.2f, MultiplicationOperator.getInstance(), 0, Condition.ALWAYS_TRUE), // Mul 1.2 (Order 2)
				new SimpleAttribute(DefaultAttributes.ATTACK_DAMAGE, 1f, SubtractionOperator.getInstance(), 0, Condition.ALWAYS_TRUE),    // Sub 1 (Order 1)

				// Group 1: (Result of Group 0: 4.8 + 10) * 2 = 14.8 * 2 = 29.6
				new SimpleAttribute(DefaultAttributes.ATTACK_DAMAGE, 2f, MultiplicationOperator.getInstance(), 1, Condition.ALWAYS_TRUE), // Mul 2 (Order 2)
				new SimpleAttribute(DefaultAttributes.ATTACK_DAMAGE, 10f, AdditionOperator.getInstance(), 1, Condition.ALWAYS_TRUE)        // Add 10 (Order 1)
		);
		var chain = new ComputationChain(attributes);

		// Detailed calculation with new precedence (Add/Sub: Order 1, Mul/Div: Order 2):
		// Group 0 (initial base 0):
		// Attributes sorted: [Add 5f, Sub 1f, Mul 1.2f]
		// 1. Add 5f: 0 + 5 = 5
		// 2. Sub 1f: 5 - 1 = 4
		// 3. Mul 1.2f: 4 * 1.2 = 4.8
		// Result of Group 0 = 4.8f

		// Group 1 (initial base 4.8f):
		// Attributes sorted: [Add 10f, Mul 2f]
		// 1. Add 10f: 4.8 + 10 = 14.8
		// 2. Mul 2f: 14.8 * 2 = 29.6
		// Final result = 29.6f
		assertEquals(29.6f, chain.compute(0f), 0.001f, new CalculationVisualizer(chain.orderedAttributes()).visualize(0f));
	}

	@Test
	void handlesEmptyAttributeList() {
		var chain = new ComputationChain(List.of());
		assertEquals(10f, chain.compute(10f));
	}

	@Test
	void usesBuilderCorrectly() {
		var builder = new ComputationChain.Builder();
		builder.addAttribute(new SimpleAttribute(DefaultAttributes.ATTACK_DAMAGE, 10f)); // Group 0, Add (Order 1)
		builder.addAttribute(new SimpleAttribute(DefaultAttributes.ATTACK_DAMAGE, 2f, MultiplicationOperator.getInstance(), 1, Condition.ALWAYS_TRUE)); // Group 1, Multiply (Order 2)
		var chain = builder.build();

		// Calculation:
		// Group 0 (initial 0): [Add 10f] -> 0 + 10 = 10
		// Group 1 (initial 10): [Mul 2f] -> 10 * 2 = 20
		assertEquals(20f, chain.compute(0f));
	}

	@Test
	void computesWithMinOperator() {
		var attributes = List.of(
				new SimpleAttribute(DefaultAttributes.ATTACK_DAMAGE, 100f, AdditionOperator.getInstance(), 0, Condition.ALWAYS_TRUE), // Add 100 (Order 1) -> 100
				new SimpleAttribute(DefaultAttributes.ATTACK_DAMAGE, 5f, MinOperator.getInstance(), 0, Condition.ALWAYS_TRUE),       // Min 5 (Order 3) -> min(100, 5) = 5
				new SimpleAttribute(DefaultAttributes.ATTACK_DAMAGE, 15f, MinOperator.getInstance(), 0, Condition.ALWAYS_TRUE)      // Min 15 (Order 3) -> min(5, 15) = 5
		);
		var chain = new ComputationChain(attributes);
		// Calculation: 0 + 100 = 100. Then min(100, 5) = 5. Then min(5, 15) = 5.
		assertEquals(5f, chain.compute(0f),  new CalculationVisualizer(chain.orderedAttributes()).visualize(0f));
	}

	@Test
	void computesWithMaxOperator() {
		var attributes = List.of(
				new SimpleAttribute(DefaultAttributes.ATTACK_DAMAGE, 1f, AdditionOperator.getInstance(), 0, Condition.ALWAYS_TRUE),    // Add 1 (Order 1) -> 1
				new SimpleAttribute(DefaultAttributes.ATTACK_DAMAGE, 12f, MaxOperator.getInstance(), 0, Condition.ALWAYS_TRUE), // Max 12 (Order 3) -> max(1, 12) = 12
				new SimpleAttribute(DefaultAttributes.ATTACK_DAMAGE, 8f, MinOperator.getInstance(), 0, Condition.ALWAYS_TRUE)    // Min 8 (Order 3) -> min(12, 8) = 8
		);
		var chain = new ComputationChain(attributes);
		// Calculation: 0 + 1 = 1. Then max(1, 12) = 12. Then min(12, 8) = 8.
		assertEquals(8f, chain.compute(0f),  new CalculationVisualizer(chain.orderedAttributes()).visualize(0f));
	}

	@Test
	void computesWithDivisionOperator() {
		var attributes = List.of(
				new SimpleAttribute(DefaultAttributes.ATTACK_DAMAGE, 100f, AdditionOperator.getInstance(), 0, Condition.ALWAYS_TRUE), // Add 100 (Order 1)
				new SimpleAttribute(DefaultAttributes.ATTACK_DAMAGE, 4f, DivisionOperator.getInstance(), 0, Condition.ALWAYS_TRUE)     // Div 4 (Order 2)
		);
		var chain = new ComputationChain(attributes);
		// Calculation: 0 + 100 = 100. Then 100 / 4 = 25.
		assertEquals(25f, chain.compute(0f), new CalculationVisualizer(chain.orderedAttributes()).visualize(0f));
	}

	@Test
	void computesWithMixedOperatorsAndMultipleGroupsComplex() {
		// Initial value = 0

		var attributes = List.of(
				// Group 0: (0 + 2) * 4 / 2 = 2 * 4 / 2 = 4
				// Then clamp result: max(4, 10) = 10, min(10, 5) = 5
				new SimpleAttribute(DefaultAttributes.ATTACK_DAMAGE, 10f, MaxOperator.getInstance(), 0, Condition.ALWAYS_TRUE),   // Max 10 (Order 3)
				new SimpleAttribute(DefaultAttributes.ATTACK_DAMAGE, 2f, AdditionOperator.getInstance(), 0, Condition.ALWAYS_TRUE),  // Add 2 (Order 1)
				new SimpleAttribute(DefaultAttributes.ATTACK_DAMAGE, 5f, MinOperator.getInstance(), 0, Condition.ALWAYS_TRUE),    // Min 5 (Order 3)
				new SimpleAttribute(DefaultAttributes.ATTACK_DAMAGE, 4f, MultiplicationOperator.getInstance(), 0, Condition.ALWAYS_TRUE), // Mul 4 (Order 2)
				new SimpleAttribute(DefaultAttributes.ATTACK_DAMAGE, 2f, DivisionOperator.getInstance(), 0, Condition.ALWAYS_TRUE),   // Div 2 (Order 2)

				// Group 1: (Result of Group 0: 5 + 3) * 2 = 8 * 2 = 16
				new SimpleAttribute(DefaultAttributes.ATTACK_DAMAGE, 3f, AdditionOperator.getInstance(), 1, Condition.ALWAYS_TRUE),  // Add 3 (Order 1)
				new SimpleAttribute(DefaultAttributes.ATTACK_DAMAGE, 2f, MultiplicationOperator.getInstance(), 1, Condition.ALWAYS_TRUE) // Mul 2 (Order 2)
		);
		var chain = new ComputationChain(attributes);

		// Detailed calculation with new precedence (Add/Sub: 1, Mul/Div: 2, Min/Max: 3):
		// Group 0 processing (initial base 0):
		// Attributes sorted: [Add 2f, Mul 4f, Div 2f, Max 10f, Min 5f]
		// 1. Add 2f: 0 + 2 = 2
		// 2. Mul 4f: 2 * 4 = 8
		// 3. Div 2f: 8 / 2 = 4
		// 4. Max 10f: Math.max(4, 10) = 10
		// 5. Min 5f: Math.min(10, 5) = 5
		// Result of Group 0 = 5.0f

		// Group 1 processing (initial base 5.0f):
		// Attributes sorted: [Add 3f, Mul 2f]
		// 1. Add 3f: 5.0 + 3 = 8.0
		// 2. Mul 2f: 8.0 * 2 = 16.0
		// Final Result = 16.0f
		assertEquals(16.0f, chain.compute(0f), 0.001f,  new CalculationVisualizer(chain.orderedAttributes()).visualize(0f));
	}

	@Test
	void computesWithNegativeValuesAndSubtraction() {
		var attributes = List.of(
				new SimpleAttribute(DefaultAttributes.ATTACK_DAMAGE, 20f, AdditionOperator.getInstance(), 0, Condition.ALWAYS_TRUE),    // Add 20 (Order 1)
				new SimpleAttribute(DefaultAttributes.ATTACK_DAMAGE, 5f, SubtractionOperator.getInstance(), 0, Condition.ALWAYS_TRUE), // Sub 5 (Order 1)
				new SimpleAttribute(DefaultAttributes.ATTACK_DAMAGE, -2f, AdditionOperator.getInstance(), 0, Condition.ALWAYS_TRUE)   // Add -2 (Order 1)
		);
		var chain = new ComputationChain(attributes);
		// Sorted: [Add 20f, Sub 5f, Add -2f] (or permutations for same order)
		// Calculation: 0 + 20 = 20. Then 20 - 5 = 15. Then 15 + (-2) = 13.
		assertEquals(13f, chain.compute(0f),  new CalculationVisualizer(chain.orderedAttributes()).visualize(0f));
	}

	@Test
	void computesClampingValueWithMinMaxAcrossGroups() {
		var attributes = List.of(
				// Group 0: Calculate initial value (0 + 50) * 2 = 100
				new SimpleAttribute(DefaultAttributes.ATTACK_DAMAGE, 50f, AdditionOperator.getInstance(), 0, Condition.ALWAYS_TRUE),    // Add 50 (Order 1)
				new SimpleAttribute(DefaultAttributes.ATTACK_DAMAGE, 2f, MultiplicationOperator.getInstance(), 0, Condition.ALWAYS_TRUE), // Mul 2 (Order 2)

				// Group 1: Apply lower bound (Max) to result of Group 0. Max(100, 20) = 100
				new SimpleAttribute(DefaultAttributes.ATTACK_DAMAGE, 20f, MaxOperator.getInstance(), 1, Condition.ALWAYS_TRUE),     // Max 20 (Order 3)

				// Group 2: Apply upper bound (Min) to result of Group 1. Min(100, 75) = 75
				new SimpleAttribute(DefaultAttributes.ATTACK_DAMAGE, 75f, MinOperator.getInstance(), 2, Condition.ALWAYS_TRUE)      // Min 75 (Order 3)
		);
		var chain = new ComputationChain(attributes);
		// Detailed calculation:
		// Group 0 (initial 0):
		// Sorted: [Add 50f, Mul 2f]
		// 1. Add 50f: 0 + 50 = 50
		// 2. Mul 2f: 50 * 2 = 100
		// Result of Group 0 = 100f

		// Group 1 (initial 100f):
		// Sorted: [Max 20f]
		// 1. Max 20f: Math.max(100, 20) = 100
		// Result of Group 1 = 100f

		// Group 2 (initial 100f):
		// Sorted: [Min 75f]
		// 1. Min 75f: Math.min(100, 75) = 75
		// Result of Group 2 = 75f
		assertEquals(75f, chain.compute(0f),  new CalculationVisualizer(chain.orderedAttributes()).visualize(0f));
	}


	@Test
	void computesWithZeroDivisionHandled() {
		var attributes = List.of(
				new SimpleAttribute(DefaultAttributes.ATTACK_DAMAGE, 10f, AdditionOperator.getInstance(), 0, Condition.ALWAYS_TRUE), // 0 + 10 = 10
				new SimpleAttribute(DefaultAttributes.ATTACK_DAMAGE, 0f, DivisionOperator.getInstance(), 0, Condition.ALWAYS_TRUE)     // 10 / 0 = 10 (returns base if divisor is 0)
		);
		var chain = new ComputationChain(attributes);
		assertThrows(ArithmeticException.class, () -> chain.compute(0f));
	}

	@Test
	void testComputationChain_withCompositeAttributeAndSimpleAttribute() {
		// 1. Create components for a CompositeAttribute
		OpenIdentifier compositeType = DefaultAttributes.ATTACK_DAMAGE;
		OpenIdentifier compositeKey = new OpenIdentifier("forgero", "tool_bonus");

		CompositeAttributeComponent comp1 = new CompositeAttributeComponent(compositeType, 5f, AdditionOperator.getInstance(), 0, compositeKey); // 0 + 5 = 5
		CompositeAttributeComponent comp2 = new CompositeAttributeComponent(compositeType, 1.5f, MultiplicationOperator.getInstance(), 1, compositeKey); // 5 * 1.5 = 7.5
		CompositeAttributeComponent comp3 = new CompositeAttributeComponent(compositeType, 2f, SubtractionOperator.getInstance(), 2, compositeKey); // 7.5 - 2 = 5.5

		// 2. Create the CompositeAttribute from these components (internal value 5.5)
		Optional<CompositeAttribute> optionalCompositeAttribute = CompositeAttribute.of(compositeType, compositeKey, List.of(comp1, comp2, comp3));
		assertTrue(optionalCompositeAttribute.isPresent(), "CompositeAttribute should be created successfully");
		CompositeAttribute compositeAttr = optionalCompositeAttribute.get();

		// Verify the internal value of the composite attribute
		assertEquals(5.5f, compositeAttr.value(), 0.001f);

		// 3. Create some SimpleAttributes
		SimpleAttribute simpleAttr1 = new SimpleAttribute(compositeType, 10f, AdditionOperator.getInstance(), 0, Condition.ALWAYS_TRUE); // Add 10 (Order 1, Group 0)
		SimpleAttribute simpleAttr2 = new SimpleAttribute(compositeType, 2f, MultiplicationOperator.getInstance(), 1, Condition.ALWAYS_TRUE); // Mul 2 (Order 2, Group 1)

		// 4. Create the ComputationChain with the CompositeAttribute and SimpleAttributes.
		var attributesForChain = List.of(
				comp1, comp2, comp3, // Should be ignored
				simpleAttr1,     // Group 0, Add 10
				compositeAttr,   // Group 0, Add (compositeAttr's value 5.5)
				simpleAttr2      // Group 1, Mul 2
		);
		var chain = new ComputationChain(attributesForChain);

		// Detailed Calculation:
		// Initial Base Value: 0f

		// Group 0: Attributes sorted by operator order:
		//   - SimpleAttribute: Add 10f (Order 1) -> 0 + 10 = 10
		//   - CompositeAttribute: Add (value 5.5f, Order 1) -> 10 + 5.5 = 15.5
		// Current Value after Group 0: 15.5f

		// Group 1: Attributes sorted by operator order:
		//   - SimpleAttribute: Mul 2f (Order 2) -> 15.5 * 2 = 31.0
		// Final Result: 31.0f

		assertEquals(31.0f, chain.compute(0f), 0.001f, new CalculationVisualizer(chain.orderedAttributes()).visualize(0f));
	}


	/**
	 * This test demonstrates the use of CalculationVisualizer.
	 * It's disabled by default as its purpose is to print debug output, not to assert a value.
	 * Enable it manually to inspect the calculation flow.
	 */
	@Test
	@Disabled("This test is for debugging visualization, not an assertion test.")
	void debugComplexCalculationChain() {
		var attributes = List.of(
				// Group 0: (0 + 2) * 4 / 2 = 2 * 4 / 2 = 4
				// Then clamp result: max(4, 10) = 10, min(10, 5) = 5
				new SimpleAttribute(DefaultAttributes.ATTACK_DAMAGE, 10f, MaxOperator.getInstance(), 0, Condition.ALWAYS_TRUE),   // Max 10 (Order 3)
				new SimpleAttribute(DefaultAttributes.ATTACK_DAMAGE, 2f, AdditionOperator.getInstance(), 0, Condition.ALWAYS_TRUE),  // Add 2 (Order 1)
				new SimpleAttribute(DefaultAttributes.ATTACK_DAMAGE, 5f, MinOperator.getInstance(), 0, Condition.ALWAYS_TRUE),    // Min 5 (Order 3)
				new SimpleAttribute(DefaultAttributes.ATTACK_DAMAGE, 4f, MultiplicationOperator.getInstance(), 0, Condition.ALWAYS_TRUE), // Mul 4 (Order 2)
				new SimpleAttribute(DefaultAttributes.ATTACK_DAMAGE, 2f, DivisionOperator.getInstance(), 0, Condition.ALWAYS_TRUE),   // Div 2 (Order 2)

				// Group 1: (Result of Group 0: 5 + 3) * 2 = 8 * 2 = 16
				new SimpleAttribute(DefaultAttributes.ATTACK_DAMAGE, 3f, AdditionOperator.getInstance(), 1, Condition.ALWAYS_TRUE),  // Add 3 (Order 1)
				new SimpleAttribute(DefaultAttributes.ATTACK_DAMAGE, 2f, MultiplicationOperator.getInstance(), 1, Condition.ALWAYS_TRUE) // Mul 2 (Order 2)
		);

		CalculationVisualizer visualizer = new CalculationVisualizer(attributes);
		String debugOutput = visualizer.visualize(0f);
		System.out.println(debugOutput);

		// You can still assert the final value here if you want to verify the calculation
		assertEquals(16.0f, new ComputationChain(new ComputationChain(attributes).orderedAttributes()).compute(0f), 0.001f);
	}
}
