package com.sigmundgranaas.forgero.core.attribute.computation;

import com.sigmundgranaas.forgero.core.attribute.api.Attribute;
import java.util.Comparator;
import java.util.List;

/**
 * A robust, ordered computation system that processes a list of active attributes.
 * This class represents the "execution" phase of a calculation, operating on a
 * pre-filtered set of attributes.
 */
public class ComputationChain {
	private final List<Attribute> orderedAttributes;

	/**
	 * Constructs a computation chain from a list of attributes that are already
	 * active for the current context. The attributes are sorted by their level upon creation.
	 *
	 * @param activeAttributes The list of contextually-active attributes to be computed.
	 */
	public ComputationChain(List<Attribute> activeAttributes) {
		this.orderedAttributes = activeAttributes.stream()
				.sorted(Comparator.comparingInt(Attribute::level))
				.toList();
	}

	/**
	 * Executes the computation chain.
	 *
	 * @param baseValue The starting value for the computation, typically 0.
	 * @return The final value after all attribute computations have been applied.
	 */
	public float compute(float baseValue) {
		float currentValue = baseValue;
		for (Attribute attribute : orderedAttributes) {
			currentValue = attribute.operator().apply(currentValue, attribute.value());
		}
		return currentValue;
	}

	/**
	 * A fluent builder for constructing a ComputationChain.
	 * This is more for future use, as chains are currently created on-the-fly.
	 */
	public static class Builder {
		private final java.util.List<Attribute> attributes = new java.util.ArrayList<>();

		public Builder addAttribute(Attribute attribute) {
			this.attributes.add(attribute);
			return this;
		}

		public ComputationChain build() {
			return new ComputationChain(attributes);
		}
	}
}
