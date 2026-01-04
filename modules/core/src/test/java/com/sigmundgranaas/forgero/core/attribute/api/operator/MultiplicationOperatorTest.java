package com.sigmundgranaas.forgero.core.attribute.api.operator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MultiplicationOperatorTest {

	@Test
	void appliesMultiplicationCorrectly() {
		MultiplicationOperator operator = MultiplicationOperator.getInstance();

		assertEquals(50.0f, operator.apply(10.0f, 5.0f));
		assertEquals(-25.0f, operator.apply(-5.0f, 5.0f));
		assertEquals(25.0f, operator.apply(-5.0f, -5.0f));
		assertEquals(2500.0f, operator.apply(50.0f, 50.0f));
	}

	@Test
	void handlesZeroValues() {
		MultiplicationOperator operator = MultiplicationOperator.getInstance();

		assertEquals(0.0f, operator.apply(0.0f, 5.0f));
		assertEquals(0.0f, operator.apply(10.0f, 0.0f));
		assertEquals(0.0f, operator.apply(0.0f, 0.0f));
	}

	@Test
	void handlesIdentityValue() {
		MultiplicationOperator operator = MultiplicationOperator.getInstance();

		// Multiplying by 1.0 should return the base value
		assertEquals(10.0f, operator.apply(10.0f, 1.0f));
		assertEquals(-5.5f, operator.apply(-5.5f, 1.0f));
	}

	@Test
	void handlesFractionalMultipliers() {
		MultiplicationOperator operator = MultiplicationOperator.getInstance();

		assertEquals(5.0f, operator.apply(10.0f, 0.5f));
		assertEquals(2.5f, operator.apply(10.0f, 0.25f));
		assertEquals(12.0f, operator.apply(10.0f, 1.2f));
	}

	@Test
	void hasCorrectOrder() {
		MultiplicationOperator operator = MultiplicationOperator.getInstance();

		// Multiplication is order 2 (applied after addition/subtraction)
		assertEquals(2, operator.order());
	}

	@Test
	void isSingleton() {
		MultiplicationOperator first = MultiplicationOperator.getInstance();
		MultiplicationOperator second = MultiplicationOperator.getInstance();

		assertSame(first, second, "MultiplicationOperator should be a singleton");
	}
}
