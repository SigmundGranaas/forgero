package com.sigmundgranaas.forgero.core.attribute.api.operator;

public class SubtractionOperator implements Operator {
	private static final SubtractionOperator INSTANCE = new SubtractionOperator();
	public static SubtractionOperator getInstance() { return INSTANCE; }
	private SubtractionOperator() {}

	@Override
	public float apply(float base, float subtraction) {
		return base - subtraction;
	}

	@Override
	public int order() {
		// Subtraction has the same precedence as Addition
		return 1;
	}
}
