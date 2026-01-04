package com.sigmundgranaas.forgero.core.attribute.api.operator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MaxOperatorTest {

	@Test
	void selectsMaximumValue() {
		MaxOperator operator = MaxOperator.getInstance();

		assertEquals(10.0f, operator.apply(10.0f, 5.0f));
		assertEquals(10.0f, operator.apply(5.0f, 10.0f));
		assertEquals(-5.0f, operator.apply(-5.0f, -10.0f));
		assertEquals(5.0f, operator.apply(0.0f, 5.0f));
	}

	@Test
	void handlesEqualValues() {
		MaxOperator operator = MaxOperator.getInstance();

		assertEquals(5.0f, operator.apply(5.0f, 5.0f));
		assertEquals(-10.0f, operator.apply(-10.0f, -10.0f));
		assertEquals(0.0f, operator.apply(0.0f, 0.0f));
	}

	@Test
	void handlesNegativeValues() {
		MaxOperator operator = MaxOperator.getInstance();

		assertEquals(5.0f, operator.apply(-5.0f, 5.0f));
		assertEquals(-50.0f, operator.apply(-100.0f, -50.0f));
	}

	@Test
	void handlesExtremeValues() {
		MaxOperator operator = MaxOperator.getInstance();

		assertEquals(Float.MAX_VALUE, operator.apply(Float.MIN_VALUE, Float.MAX_VALUE));
		assertEquals(0.0f, operator.apply(Float.NEGATIVE_INFINITY, 0.0f));
		assertEquals(Float.POSITIVE_INFINITY, operator.apply(Float.POSITIVE_INFINITY, 100.0f));
	}

	@Test
	void hasCorrectOrder() {
		MaxOperator operator = MaxOperator.getInstance();

		// Max is order 4 (selection operations)
		assertEquals(4, operator.order());
	}

	@Test
	void isSingleton() {
		MaxOperator first = MaxOperator.getInstance();
		MaxOperator second = MaxOperator.getInstance();

		assertSame(first, second, "MaxOperator should be a singleton");
	}
}
