package com.sigmundgranaas.forgero.core.attribute.api.operator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MinOperatorTest {

	@Test
	void selectsMinimumValue() {
		MinOperator operator = MinOperator.getInstance();

		assertEquals(5.0f, operator.apply(10.0f, 5.0f));
		assertEquals(5.0f, operator.apply(5.0f, 10.0f));
		assertEquals(-10.0f, operator.apply(-5.0f, -10.0f));
		assertEquals(0.0f, operator.apply(0.0f, 5.0f));
	}

	@Test
	void handlesEqualValues() {
		MinOperator operator = MinOperator.getInstance();

		assertEquals(5.0f, operator.apply(5.0f, 5.0f));
		assertEquals(-10.0f, operator.apply(-10.0f, -10.0f));
		assertEquals(0.0f, operator.apply(0.0f, 0.0f));
	}

	@Test
	void handlesNegativeValues() {
		MinOperator operator = MinOperator.getInstance();

		assertEquals(-5.0f, operator.apply(-5.0f, 5.0f));
		assertEquals(-100.0f, operator.apply(-100.0f, -50.0f));
	}

	@Test
	void handlesExtremeValues() {
		MinOperator operator = MinOperator.getInstance();

		assertEquals(Float.MIN_VALUE, operator.apply(Float.MIN_VALUE, Float.MAX_VALUE));
		assertEquals(Float.NEGATIVE_INFINITY, operator.apply(Float.NEGATIVE_INFINITY, 0.0f));
	}

	@Test
	void hasCorrectOrder() {
		MinOperator operator = MinOperator.getInstance();

		// Min is order 4 (selection operations)
		assertEquals(4, operator.order());
	}

	@Test
	void isSingleton() {
		MinOperator first = MinOperator.getInstance();
		MinOperator second = MinOperator.getInstance();

		assertSame(first, second, "MinOperator should be a singleton");
	}
}
