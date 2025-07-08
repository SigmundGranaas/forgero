package com.sigmundgranaas.forgero.core.attribute.impl.computation.operator;

public class MultiplicationOperator implements Operator {
	private static final MultiplicationOperator INSTANCE = new MultiplicationOperator();
	public static MultiplicationOperator getInstance() { return INSTANCE; }
	private MultiplicationOperator() {}

	@Override
	public float apply(float base, float addition) {
		// Note: For multiplication, the 'addition' is the factor.
		return base * addition;
	}

	@Override
	public int order() {
		return 2; // Adjusted to match Division
	}
}
