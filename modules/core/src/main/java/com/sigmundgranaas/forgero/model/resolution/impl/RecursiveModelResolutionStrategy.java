package com.sigmundgranaas.forgero.model.resolution.impl;

import com.sigmundgranaas.forgero.common.identifier.api.Identifiable;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.StructuredComponent;
import com.sigmundgranaas.forgero.model.registry.api.ModelRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Reusable strategy for recursive model resolution using pure composition.
 *
 * Instead of an abstract base class, this is a composable component that
 * can be used by any resolver. This demonstrates the Strategy pattern
 * combined with composition.
 *
 * @param <M> The model type, must be Identifiable
 */
public class RecursiveModelResolutionStrategy<M extends Identifiable> {
	private final ModelRegistry<M> registry;
	private final Predicate<Component> shouldRecurse;

	/**
	 * Creates a new recursive resolution strategy.
	 *
	 * @param registry      The registry to look up models from
	 * @param shouldRecurse Predicate to determine if a component should be recursed into
	 */
	public RecursiveModelResolutionStrategy(ModelRegistry<M> registry, Predicate<Component> shouldRecurse) {
		this.registry = registry;
		this.shouldRecurse = shouldRecurse;
	}

	/**
	 * Creates a strategy with default recursion behavior (recurse into all non-empty components).
	 *
	 * @param registry The registry to look up models from
	 */
	public RecursiveModelResolutionStrategy(ModelRegistry<M> registry) {
		this(registry, component -> !component.id().path().contains("empty"));
	}

	/**
	 * Resolves a component and its children into models recursively.
	 *
	 * @param component The root component to resolve
	 * @return A list of all models found (from the component and its children)
	 */
	public List<M> resolve(Component component) {
		List<M> models = new ArrayList<>();
		resolveRecursively(component, models);
		return models;
	}

	/**
	 * Internal recursive resolution method.
	 *
	 * @param component The current component being resolved
	 * @param models    The accumulator list for found models
	 */
	private void resolveRecursively(Component component, List<M> models) {
		// Step 1: Check if the current component has its own model
		registry.find(component.id()).ifPresent(models::add);

		// Step 2: Recurse into children if this is a structured component
		if (component instanceof StructuredComponent structured) {
			structured.structure().slots().all().forEach(slot -> {
				Component child = slot.content();
				if (child != null && shouldRecurse.test(child)) {
					resolveRecursively(child, models);
				}
			});
		}
	}

	/**
	 * Creates a builder for customizing the resolution strategy.
	 */
	public static <M extends Identifiable> Builder<M> builder(ModelRegistry<M> registry) {
		return new Builder<>(registry);
	}

	/**
	 * Builder for RecursiveModelResolutionStrategy using composition.
	 */
	public static class Builder<M extends Identifiable> {
		private final ModelRegistry<M> registry;
		private Predicate<Component> shouldRecurse = component -> !component.id().path().contains("empty");

		private Builder(ModelRegistry<M> registry) {
			this.registry = registry;
		}

		/**
		 * Sets a custom predicate for determining which components to recurse into.
		 */
		public Builder<M> shouldRecurse(Predicate<Component> predicate) {
			this.shouldRecurse = predicate;
			return this;
		}

		/**
		 * Builds the strategy.
		 */
		public RecursiveModelResolutionStrategy<M> build() {
			return new RecursiveModelResolutionStrategy<>(registry, shouldRecurse);
		}
	}
}
