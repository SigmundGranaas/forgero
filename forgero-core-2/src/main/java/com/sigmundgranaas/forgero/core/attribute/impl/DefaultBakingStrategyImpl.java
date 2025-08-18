package com.sigmundgranaas.forgero.core.attribute.impl;

import com.sigmundgranaas.forgero.core.attribute.api.Attribute;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.property.condition.Condition;
import com.sigmundgranaas.forgero.core.property.context.ResolutionContext;

import java.util.List;
import java.util.stream.Stream;

import static com.sigmundgranaas.forgero.core.attribute.api.Attribute.KEY;

/**
 * Default implementation of {@link AttributeBakingStrategy} that processes attributes
 * without special handling for composite attributes from structured components.
 * This effectively matches the previous behavior of {@link com.sigmundgranaas.forgero.core.attribute.impl.AttributeEngine#bake}.
 */
public class DefaultBakingStrategyImpl implements AttributeBakingStrategy {
	@Override
	public List<Attribute> bake(Stream<Component> components) {
		List<Component> componentList = components.toList();
		// Assuming the first component in the list is the root for context resolution.
		Component root = componentList.isEmpty() ? null : componentList.get(0);

		return componentList.stream()
				.flatMap(component -> component.properties(KEY)
						.stream()
						.filter(attribute -> {
							// Apply static conditions using a ResolutionContext specific to this component.
							ResolutionContext resCtx = new ResolutionContext(component, root);
							return attribute.condition()
									.map(Condition::staticConditions)
									.map(resCtx::test)
									.orElse(true); // If no static conditions, it passes.
						}))
				.toList();
	}
}
