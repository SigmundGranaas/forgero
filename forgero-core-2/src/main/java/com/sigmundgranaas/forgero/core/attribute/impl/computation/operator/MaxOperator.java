package com.sigmundgranaas.forgero.core.attribute.impl.computation.operator;

public class MaxOperator implements Operator {
	private static final MaxOperator INSTANCE = new MaxOperator();
	public static MaxOperator getInstance() { return INSTANCE; }
	private MaxOperator() {}

	@Override
	public float apply(float base, float valueToCompare) {
		return Math.max(base, valueToCompare);
	}

	@Override
	public int order() {
		// Min/Max operators often act as clamps or selectors, usually applied very early.
		return 4; // Highest precedence (lowest number)
	}
}
