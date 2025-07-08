package com.sigmundgranaas.forgero.core.attribute.impl.computation.operator;

public class MinOperator implements Operator {
	private static final MinOperator INSTANCE = new MinOperator();
	public static MinOperator getInstance() { return INSTANCE; }
	private MinOperator() {}

	@Override
	public float apply(float base, float valueToCompare) {
		return Math.min(base, valueToCompare);
	}

	@Override
	public int order() {
		// Min/Max operators often act as clamps or selectors, usually applied very early.
		return 4; // Highest precedence (lowest number)
	}
}
