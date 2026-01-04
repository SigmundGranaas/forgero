package com.sigmundgranaas.forgero.core.attribute.api.operator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AdditionOperatorTest {

	@Test
	void appliesAdditionCorrectly() {
		AdditionOperator operator = AdditionOperator.getInstance();

		assertEquals(15.0f, operator.apply(10.0f, 5.0f));
		assertEquals(0.0f, operator.apply(-5.0f, 5.0f));
		assertEquals(-10.0f, operator.apply(-5.0f, -5.0f));
		assertEquals(100.5f, operator.apply(50.25f, 50.25f));
	}

	@Test
	void handlesZeroValues() {
		AdditionOperator operator = AdditionOperator.getInstance();

		assertEquals(5.0f, operator.apply(0.0f, 5.0f));
		assertEquals(10.0f, operator.apply(10.0f, 0.0f));
		assertEquals(0.0f, operator.apply(0.0f, 0.0f));
	}

	@Test
	void handlesLargeValues() {
		AdditionOperator operator = AdditionOperator.getInstance();

		float large = 1_000_000.0f;
		assertEquals(2_000_000.0f, operator.apply(large, large));

		float veryLarge = Float.MAX_VALUE / 2;
		float result = operator.apply(veryLarge, veryLarge);
		assertTrue(result > veryLarge); // Should be larger, but not overflow to infinity
		assertFalse(Float.isInfinite(result));
	}

	@Test
	void hasCorrectOrder() {
		AdditionOperator operator = AdditionOperator.getInstance();

		// Addition is order 1 (base operations, before multiplication)
		assertEquals(1, operator.order());
	}

	@Test
	void isSingleton() {
		AdditionOperator first = AdditionOperator.getInstance();
		AdditionOperator second = AdditionOperator.getInstance();

		assertSame(first, second, "AdditionOperator should be a singleton");
	}
}
