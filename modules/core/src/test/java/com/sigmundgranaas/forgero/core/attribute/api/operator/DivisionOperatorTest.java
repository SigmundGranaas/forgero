package com.sigmundgranaas.forgero.core.attribute.api.operator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DivisionOperatorTest {

	@Test
	void appliesDivisionCorrectly() {
		DivisionOperator operator = DivisionOperator.getInstance();

		assertEquals(2.0f, operator.apply(10.0f, 5.0f));
		assertEquals(-1.0f, operator.apply(-5.0f, 5.0f));
		assertEquals(1.0f, operator.apply(-5.0f, -5.0f));
		assertEquals(1.0f, operator.apply(50.0f, 50.0f));
	}

	@Test
	void handlesIdentityValue() {
		DivisionOperator operator = DivisionOperator.getInstance();

		// Dividing by 1.0 should return the base value
		assertEquals(10.0f, operator.apply(10.0f, 1.0f));
		assertEquals(-5.5f, operator.apply(-5.5f, 1.0f));
	}

	@Test
	void handlesFractionalDivisors() {
		DivisionOperator operator = DivisionOperator.getInstance();

		assertEquals(20.0f, operator.apply(10.0f, 0.5f));
		assertEquals(40.0f, operator.apply(10.0f, 0.25f));
	}

	@Test
	void handlesDivisionByZero() {
		DivisionOperator operator = DivisionOperator.getInstance();

		// Division by zero throws ArithmeticException
		assertThrows(ArithmeticException.class, () -> operator.apply(10.0f, 0.0f));
		assertThrows(ArithmeticException.class, () -> operator.apply(-10.0f, 0.0f));
		assertThrows(ArithmeticException.class, () -> operator.apply(0.0f, 0.0f));
	}

	@Test
	void handlesZeroNumerator() {
		DivisionOperator operator = DivisionOperator.getInstance();

		assertEquals(0.0f, operator.apply(0.0f, 5.0f));
		assertEquals(0.0f, operator.apply(0.0f, 100.0f));
	}

	@Test
	void hasCorrectOrder() {
		DivisionOperator operator = DivisionOperator.getInstance();

		// Division is order 3
		assertEquals(3, operator.order());
	}

	@Test
	void isSingleton() {
		DivisionOperator first = DivisionOperator.getInstance();
		DivisionOperator second = DivisionOperator.getInstance();

		assertSame(first, second, "DivisionOperator should be a singleton");
	}
}
