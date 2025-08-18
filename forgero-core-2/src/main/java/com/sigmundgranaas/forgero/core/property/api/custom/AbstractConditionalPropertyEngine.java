package com.sigmundgranaas.forgero.core.property.api.custom;

import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.property.api.DataTypeEngine;
import com.sigmundgranaas.forgero.core.property.api.PropertyKey;
import com.sigmundgranaas.forgero.core.property.api.ResolutionKey;
import com.sigmundgranaas.forgero.core.property.condition.Condition;
import com.sigmundgranaas.forgero.core.property.context.ResolutionContext;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * An abstract base class for a DataTypeEngine that resolves a ConditionalProperty.
 * It fully implements the bake phase, including filtering by type, checking static
 * conditions, and partitioning into static/dynamic lists for optimized resolution.
 *
 * @param <P> The concrete ConditionalProperty type this engine handles.
 * @param <R> The final result type this engine produces.
 */
public abstract class AbstractConditionalPropertyEngine<P extends ConditionalProperty, R> implements DataTypeEngine<OptimizedBakedResult<P>, R> {

	private final ResolutionKey<R> key;
	private final PropertyKey<P> propertyKey;

	protected AbstractConditionalPropertyEngine(ResolutionKey<R> key, PropertyKey<P> propertyKey) {
		this.key = key;
		this.propertyKey = propertyKey;
	}

	@Override
	public ResolutionKey<R> key() {
		return key;
	}

	@Override
	public OptimizedBakedResult<P> bake(Stream<Component> components) {
		List<Component> componentList = components.toList();
		Component root = componentList.isEmpty() ? null : componentList.get(0);

		List<P> staticallyValid = componentList.stream()
				.flatMap(component -> {
					ResolutionContext resCtx = new ResolutionContext(component, root);
					// Use the PropertyKey to get only the relevant properties from the holder
					return component.properties(propertyKey).stream()
							.filter(prop -> prop.getCondition()
									.map(Condition::staticConditions)
									.map(resCtx::test)
									.orElse(true));
				})
				.toList();

		Map<Boolean, List<P>> partitioned = staticallyValid.stream()
				.collect(Collectors.partitioningBy(p -> p.getCondition().map(c -> c.dynamicConditions().isEmpty()).orElse(true)));

		return new OptimizedBakedResult<>(partitioned.get(true), partitioned.get(false));
	}
}
