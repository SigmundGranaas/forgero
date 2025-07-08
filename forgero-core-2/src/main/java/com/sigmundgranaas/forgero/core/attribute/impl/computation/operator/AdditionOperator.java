package com.sigmundgranaas.forgero.core.attribute.impl.computation.operator;

public class AdditionOperator implements Operator {
	private static final AdditionOperator INSTANCE = new AdditionOperator();
	public static AdditionOperator getInstance() { return INSTANCE; }
	private AdditionOperator() {}

	@Override
	public float apply(float base, float addition) {
		return base + addition;
	}

	@Override
	public int order() {
		// Additions should be performed first
		return 1;
	}
}
