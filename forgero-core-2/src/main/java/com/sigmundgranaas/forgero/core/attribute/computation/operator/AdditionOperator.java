package com.sigmundgranaas.forgero.core.attribute.computation.operator;

public class AdditionOperator implements Operator {
	private static final AdditionOperator INSTANCE = new AdditionOperator();
	public static AdditionOperator getInstance() { return INSTANCE; }
	private AdditionOperator() {}

	@Override
	public float apply(float base, float addition) {
		return base + addition;
	}
}
