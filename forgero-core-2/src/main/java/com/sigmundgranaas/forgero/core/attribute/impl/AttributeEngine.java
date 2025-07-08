package com.sigmundgranaas.forgero.core.attribute.impl;

import com.sigmundgranaas.forgero.core.attribute.api.Attribute;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.StructuredComponent;
import com.sigmundgranaas.forgero.core.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.property.api.DataTypeEngine;
import com.sigmundgranaas.forgero.core.property.api.ResolutionKey;
import com.sigmundgranaas.forgero.core.attribute.api.AttributeQueryResult;
import com.sigmundgranaas.forgero.core.attribute.impl.computation.ComputationChain;
import com.sigmundgranaas.forgero.core.property.condition.Condition;
import com.sigmundgranaas.forgero.core.property.context.DynamicContext;

import java.util.List;
import java.util.stream.Stream;

/**
 * The expert engine for resolving Attributes.
 * This class now uses a Strategy Pattern for its `bake` method to allow for different
 * attribute processing logic based on the type of component being resolved.
 * <p>
 * Intermediate Baked Type {@code <B>}: {@code List<Attribute>} - This list contains all attributes from the component
 * tree that have passed their static conditions.
 * <p>
 * Final Result Type {@code <R>}: {@link AttributeQueryResult} - This is a lightweight, context-aware object
 * that can be queried for the final value of any specific attribute.
 */
public class AttributeEngine implements DataTypeEngine<List<Attribute>, AttributeQueryResult> {
	public static final ResolutionKey<AttributeQueryResult> KEY = new ResolutionKey<>(new OpenIdentifier("forgero", "attributes"));

	// Initialize the concrete strategies
	private final AttributeBakingStrategy defaultBakingStrategy = new DefaultBakingStrategyImpl();
	private final AttributeBakingStrategy compositeBakingStrategy = new CompositeAttributeBakingStrategy();

	private final boolean ignoreComponents;

	public AttributeEngine() {
		ignoreComponents = true;
	}

	public AttributeEngine(boolean ignoreComponents) {
		this.ignoreComponents = ignoreComponents;
	}

	@Override
	public ResolutionKey<AttributeQueryResult> key() {
		return KEY;
	}

	@Override
	public List<Attribute> bake(Stream<Component> components) {
		// Convert the stream to a list to be able to inspect the first element (root component).
		// This allows the engine to choose the appropriate baking strategy.
		List<Component> componentList = components.toList(); // Consume stream once to inspect root

		// Determine which baking strategy to use based on the type of the root component.
		// If the main component is structured, use the strategy that handles composite attributes.
		// Otherwise, use the default strategy.
		AttributeBakingStrategy strategyToUse;
		if (!componentList.isEmpty() && componentList.get(0) instanceof StructuredComponent) {
			strategyToUse = compositeBakingStrategy;
		} else {
			strategyToUse = defaultBakingStrategy;
		}

		// Delegate the baking process to the chosen strategy. Pass the list's stream to ensure it's iterable.
		return strategyToUse.bake(componentList.stream());
	}

	@Override
	public AttributeQueryResult apply(List<Attribute> staticallyValidAttributes, DynamicContext context) {
		// Return a lightweight, context-aware query object (implemented as a lambda).
		// This object captures the list of statically valid attributes and the dynamic context.
		return (attributeType) -> {
			List<Attribute> activeAttributes = staticallyValidAttributes.stream()
					// 1. Filter for the specific attribute type we want to compute now.
					.filter(attribute -> attribute.type().equals(attributeType))
					// 2. Filter by evaluating all dynamic conditions against the provided context.
					//    Safely handle optional conditions and ensure all dynamic conditions pass.
					.filter(attribute -> attribute.condition()
							.map(Condition::dynamicConditions) // Get the list of dynamic conditions
							.map(dynamicConditions -> dynamicConditions.stream().allMatch(cond -> cond.test(context))) // Test all
							.orElse(true) // If no dynamic conditions, it passes by default
					)
					.toList();

			// 3. Run the computation chain on the final, small list of active attributes.
			//    The ComputationChain constructor will further sort these attributes by group and operator precedence.
			return new ComputationChain(activeAttributes).compute(0f);
		};
	}
}
