package com.sigmundgranaas.forgero.core.attribute.impl.computation.operator;

/**
 * A strategy interface for a mathematical operation.
 * Each implementation defines how to combine a base value with a new value.
 */
@FunctionalInterface
public interface Operator {
	float apply(float base, float addition);

	default int order(){
		return 1;
	}
}
