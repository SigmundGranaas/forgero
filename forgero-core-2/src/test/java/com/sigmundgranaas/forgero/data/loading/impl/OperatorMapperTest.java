package com.sigmundgranaas.forgero.data.loading.impl;

import com.sigmundgranaas.forgero.core.attribute.api.operator.AdditionOperator;
import com.sigmundgranaas.forgero.core.attribute.api.operator.MultiplicationOperator;
import com.sigmundgranaas.forgero.core.attribute.api.operator.Operator;
import com.sigmundgranaas.forgero.data.loading.impl.codec.AttributeCodecs;
import com.sigmundgranaas.forgero.data.loading.impl.codec.OperatorMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class OperatorMapperTest {

	private OperatorMapper mapper;

	@BeforeEach
	void setUp() {
		mapper = new OperatorMapper();
	}

	@Test
	void testMapAdditionOperator() {
		Operator op = mapper.apply(AttributeCodecs.ADDITION_OPERATOR);
		assertInstanceOf(AdditionOperator.class, op);
	}

	@Test
	void testMapMultiplicationOperator() {
		Operator op = mapper.apply(AttributeCodecs.MULTIPLICATION_OPERATOR);
		assertInstanceOf(MultiplicationOperator.class, op);
	}

	@Test
	void testMapUnknownOperatorDefaultsToAddition() {
		Operator op = mapper.apply("forgero:unknown_operator");
		assertInstanceOf(AdditionOperator.class, op, "Should default to AdditionOperator for unknown strings");
	}

	@Test
	void testMapLeveledOrder() {
		assertEquals(0, mapper.leveledOrder(AttributeCodecs.BASE_ORDER));
		assertEquals(1, mapper.leveledOrder(AttributeCodecs.MIDDLE_ORDER));
		assertEquals(2, mapper.leveledOrder(AttributeCodecs.END_ORDER));
	}

	@Test
	void testMapUnknownOrderDefaultsToMiddle() {
		assertEquals(1, mapper.leveledOrder("forgero:unknown_order"), "Should default to 1 for unknown order strings");
	}
}
