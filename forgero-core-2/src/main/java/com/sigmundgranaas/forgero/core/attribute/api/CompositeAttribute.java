package com.sigmundgranaas.forgero.core.attribute.api;

import com.sigmundgranaas.forgero.core.attribute.impl.computation.ComputationChain;
import com.sigmundgranaas.forgero.core.attribute.api.operator.AdditionOperator;
import com.sigmundgranaas.forgero.core.attribute.api.operator.Operator;
import com.sigmundgranaas.forgero.core.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.property.condition.Condition;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents a composite attribute formed by aggregating multiple {@link CompositeAttributeComponent}s.
 * This record combines the values of its constituent components into a single computed value,
 * respecting their individual operators and groups through an internal {@link ComputationChain}.
 * <p>
 * A CompositeAttribute is considered "complete" and valid only if it is constructed from
 * a list of {@link CompositeAttributeComponent}s that share the same type and composite key,
 * and crucially, include components with at least two different {@link com.sigmundgranaas.forgero.core.attribute.computation.operator.Operator} types.
 *
 * <p><b>Creation Context</b></p>
 * Within the Forgero ecosystem, CompositeAttributes are typically created by the {@link com.sigmundgranaas.forgero.core.attribute.impl.CompositeAttributeBakingStrategy}.
 * This strategy enforces that the constituent {@link CompositeAttributeComponent}s must all originate from the
 * <strong>same parent component</strong> (e.g., all from an 'iron-pickaxe_head'). This ensures that composite calculations
 * are encapsulated and predictable.
 * <p>
 * This record implements {@link Attribute} but does NOT implement {@link AttributeComponent}.
 * This distinction is critical for the attribute computation system to allow for filtering
 * out individual {@link AttributeComponent}s once they have been successfully aggregated
 * into a {@link CompositeAttribute}.
 */
public record CompositeAttribute(
		OpenIdentifier type,
		OpenIdentifier compositeKey,
		List<CompositeAttributeComponent> composites,
		Operator operator,
		int group
) implements Attribute {

	/**
	 * Private canonical constructor to enforce creation via the static factory method {@link #of(OpenIdentifier, OpenIdentifier, List)}.
	 * This constructor ensures immutability of the 'composites' list by making a defensive copy.
	 * It assumes all validation has already been performed by the factory method.
	 */
	public CompositeAttribute {
		// Ensure immutability of the composites list
		composites = List.copyOf(composites);
	}

	/**
	 * Factory method to create a new CompositeAttribute instance.
	 * This method performs all necessary validation on the input components.
	 * If the components do not meet the criteria for a valid CompositeAttribute,
	 * an empty Optional is returned.
	 *
	 * @param type         The common type of the attribute for this composite.
	 * @param compositeKey The common composite key that groups these components.
	 * @param components   A list of {@link CompositeAttributeComponent}s that should form this composite attribute.
	 * @return An {@link Optional} containing a new, validated CompositeAttribute instance if successful,
	 *         otherwise an empty Optional if validation fails.
	 */
	public static Optional<CompositeAttribute> of(OpenIdentifier type, OpenIdentifier compositeKey, List<CompositeAttributeComponent> components) {
		if (components.isEmpty()) {
			// Log this, or return empty Optional as per the requirement
			// System.err.println("CompositeAttribute creation failed: Must contain at least one component.");
			return Optional.empty();
		}

		// Ensure all components match the given type and compositeKey
		for (CompositeAttributeComponent component : components) {
			if (!component.type().equals(type)) { // Compare with the 'type' parameter
				return Optional.empty();
			}
			if (!component.compositeKey().equals(compositeKey)) { // Compare with the 'compositeKey' parameter
				return Optional.empty();
			}
		}

		// Enforce the requirement: "More than one compositeComponent with different operators is always needed"
		Set<Operator> distinctOperators = components.stream()
				.map(CompositeAttributeComponent::operator)
				.collect(Collectors.toSet());

		if (distinctOperators.size() < 2) {
			return Optional.empty();
		}

		// If all validations pass, create and return the new instance
		// Provide default operator and group for the CompositeAttribute itself
		return Optional.of(new CompositeAttribute(type, compositeKey, components, AdditionOperator.getInstance(), 0));
	}

	/**
	 * Computes the aggregated value of this composite attribute by applying
	 * a {@link ComputationChain} to its internal {@link CompositeAttributeComponent}s.
	 * The internal computation starts with a base value of 0.
	 *
	 * @return The final computed value of the composite attribute.
	 */
	@Override
	public float value() {
		// Compute the value from the internal components using a ComputationChain.
		// The base value for this internal computation is typically 0.
		ComputationChain chain = new ComputationChain(composites.stream().map(CompositeAttributeComponent::asComputableAttribute).toList());
		return chain.compute(0f);
	}

	@Override
	public Optional<Condition> condition() {
		// A CompositeAttribute typically doesn't have its own direct condition,
		// as the conditions of its constituent components are handled by the
		// AttributeEngine during the initial filtering (baking) phase.
		return Optional.empty();
	}

	/**
	 * Gets an unmodifiable list of the {@link CompositeAttributeComponent}s
	 * that make up this composite attribute.
	 * <p>
	 * Note: Records automatically provide accessor methods for their components (e.g., `composites()`).
	 * This method is provided for explicit naming, but `composites()` could also be used.
	 *
	 * @return An unmodifiable list of composite components.
	 */
	public List<CompositeAttributeComponent> getComposites() {
		return composites;
	}
}
