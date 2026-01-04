package com.sigmundgranaas.forgero.core.attribute.api.operator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SubtractionOperatorTest {

	@Test
	void appliesSubtractionCorrectly() {
		SubtractionOperator operator = SubtractionOperator.getInstance();

		assertEquals(5.0f, operator.apply(10.0f, 5.0f));
		assertEquals(-10.0f, operator.apply(-5.0f, 5.0f));
		assertEquals(0.0f, operator.apply(-5.0f, -5.0f));
		assertEquals(0.0f, operator.apply(50.25f, 50.25f));
	}

	@Test
	void handlesZeroValues() {
		SubtractionOperator operator = SubtractionOperator.getInstance();

		assertEquals(-5.0f, operator.apply(0.0f, 5.0f));
		assertEquals(10.0f, operator.apply(10.0f, 0.0f));
		assertEquals(0.0f, operator.apply(0.0f, 0.0f));
	}

	@Test
	void handlesNegativeResults() {
		SubtractionOperator operator = SubtractionOperator.getInstance();

		assertEquals(-5.0f, operator.apply(5.0f, 10.0f));
		assertEquals(-100.0f, operator.apply(50.0f, 150.0f));
	}

	@Test
	void hasCorrectOrder() {
		SubtractionOperator operator = SubtractionOperator.getInstance();

		// Subtraction is order 1 (base operations, before multiplication)
		assertEquals(1, operator.order());
	}

	@Test
	void isSingleton() {
		SubtractionOperator first = SubtractionOperator.getInstance();
		SubtractionOperator second = SubtractionOperator.getInstance();

		assertSame(first, second, "SubtractionOperator should be a singleton");
	}
}
