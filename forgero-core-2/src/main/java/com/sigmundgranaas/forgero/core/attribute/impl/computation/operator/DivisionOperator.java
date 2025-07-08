package com.sigmundgranaas.forgero.core.attribute.impl.computation.operator;

public class DivisionOperator implements Operator {
	private static final DivisionOperator INSTANCE = new DivisionOperator();
	public static DivisionOperator getInstance() { return INSTANCE; }
	private DivisionOperator() {}

	@Override
	public float apply(float base, float divisor) {
		if (divisor == 0f) {
			throw new ArithmeticException("Division by zero");
		}
		return base / divisor;
	}

	@Override
	public int order() {
		// Division has higher precedence than Addition/Subtraction, and equal to Multiplication
		return 3;
	}
}
