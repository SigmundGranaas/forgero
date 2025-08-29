package com.sigmundgranaas.forgero.core.attribute.impl.computation;

import com.sigmundgranaas.forgero.core.attribute.api.Attribute;
import com.sigmundgranaas.forgero.core.attribute.api.AttributeComponent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

/**
 * A robust, ordered computation system that processes a list of active attributes.
 * This class represents the "execution" phase of a calculation, operating on a
 * pre-filtered set of attributes. It supports grouping of calculations, where each
 * group is calculated in isolation, with operator precedence applied within the group.
 * <p>
 * The sorting logic is applied upon construction to optimize computation.
 */
public record ComputationChain(List<? extends Attribute> orderedAttributes, boolean ignoreComponent) {

	/**
	 * Constructs a computation chain from a list of attributes that are already
	 * active for the current context. The attributes are sorted upon creation to ensure
	 * correct calculation order based on group and operator precedence.
	 *
	 * @param orderedAttributes The list of contextually-active attributes to be computed.
	 */
	public ComputationChain(List<? extends Attribute> orderedAttributes) {
		this(orderedAttributes, true);
	}

	public ComputationChain(List<? extends Attribute> orderedAttributes, boolean ignoreComponent) {
		this.ignoreComponent = ignoreComponent;
		this.orderedAttributes = orderedAttributes.stream()
				.filter(notComponent())
				.sorted(Comparator.comparingInt(Attribute::group)
						.thenComparingInt(attr -> attr.operator().order()))
				.toList();
	}

	private Predicate<Attribute> notComponent(){
		if( ignoreComponent ){
			return (attribute) -> !(attribute instanceof AttributeComponent);
		} else {
			return   (attribute) -> true;
		}
	}

	/**
	 * Executes the computation chain on a pre-sorted list of attributes.
	 * <p>
	 * The calculation order is determined by the sorting performed during construction:
	 * 1. Attributes are processed group by group (e.g., all group 0, then all group 1).
	 * 2. Within each group, attributes are processed based on operator precedence
	 *    (e.g., multiplication before addition).
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
	 */
	public static class Builder {
		private final List<Attribute> attributes = new ArrayList<>();

		public Builder addAttribute(Attribute attribute) {
			this.attributes.add(attribute);
			return this;
		}

		public ComputationChain build() {
			// The constructor will handle the sorting.
			return new ComputationChain(attributes);
		}
	}
}
