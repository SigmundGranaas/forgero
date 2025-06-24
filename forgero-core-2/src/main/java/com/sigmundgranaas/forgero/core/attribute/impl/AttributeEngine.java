package com.sigmundgranaas.forgero.core.attribute.impl;

import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.property.api.DataTypeEngine;
import com.sigmundgranaas.forgero.core.property.api.ResolutionKey;
import com.sigmundgranaas.forgero.core.attribute.api.Attribute;
import com.sigmundgranaas.forgero.core.attribute.api.AttributeQueryResult;
import com.sigmundgranaas.forgero.core.attribute.computation.ComputationChain;
import com.sigmundgranaas.forgero.core.property.context.DynamicContext;
import com.sigmundgranaas.forgero.core.property.context.ResolutionContext;

import java.util.List;
import java.util.stream.Stream;

/**
 * The expert engine for resolving Attributes.
 * <p>
 * Intermediate Baked Type {@code <B>}: {@code List<Attribute>} - This list contains all attributes from the component
 * tree that have passed their static conditions.
 * <p>
 * Final Result Type {@code <R>}: {@link AttributeQueryResult} - This is a lightweight, context-aware object
 * that can be queried for the final value of any specific attribute.
 */
public class AttributeEngine implements DataTypeEngine<List<Attribute>, AttributeQueryResult> {
	public static final ResolutionKey<AttributeQueryResult> KEY = new ResolutionKey<>(new OpenIdentifier("forgero", "attributes"));

	@Override
	public ResolutionKey<AttributeQueryResult> key() {
		return KEY;
	}

	@Override
	public List<Attribute> bake(Stream<Component> components) {
		List<Component> componentList = components.toList();
		Component root = componentList.get(0);
		return componentList.stream()
				.flatMap(component -> component.getProperties().stream()
						.filter(Attribute.class::isInstance)
						.map(Attribute.class::cast)
						.filter(attribute -> {
							ResolutionContext resCtx = new ResolutionContext(component, root);
							return resCtx.test(attribute.condition().staticConditions());
						}))
				.toList();
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
					.filter(attribute -> attribute.condition().dynamicConditions().stream()
							.allMatch(cond -> cond.test(context)))
					.toList();

			// 3. Run the computation chain on the final, small list of active attributes.
			return new ComputationChain(activeAttributes).compute(0f);
		};
	}
}
