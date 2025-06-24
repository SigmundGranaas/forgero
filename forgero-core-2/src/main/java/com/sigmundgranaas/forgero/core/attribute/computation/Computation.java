package com.sigmundgranaas.forgero.core.attribute.computation;

import com.sigmundgranaas.forgero.core.attribute.api.Attribute;

/**
 * A functional interface representing a single, stateless computation.
 * This is a building block for the ComputationChain.
 */
@FunctionalInterface
public interface Computation {
	/**
	 * Applies a computation to a given value.
	 *
	 * @param current The current float value in the chain.
	 * @param attribute The attribute providing the value and context for this computation.
	 * @return The new value after the computation.
	 */
	float compute(float current, Attribute attribute);
}
