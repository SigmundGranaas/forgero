package com.sigmundgranaas.forgero.model.registry.impl;

import com.sigmundgranaas.forgero.common.identifier.api.Identifiable;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.model.api.Contextual;
import com.sigmundgranaas.forgero.model.registry.api.ContextualModelRegistry;
import com.sigmundgranaas.forgero.model.registry.api.ModelRegistry;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Contextual model registry implementation using pure composition.
 *
 * This class COMPOSES multiple ModelRegistry instances (one per context)
 * rather than inheriting from a base class. This demonstrates the composition
 * pattern where behavior is built by combining objects.
 *
 * @param <M> The model type, must be both Identifiable and Contextual
 */
public class CompositeContextualModelRegistry<M extends Identifiable & Contextual> implements ContextualModelRegistry<M> {
	private static final String DEFAULT_CONTEXT = "default";

	// Composition: CONTAINS multiple registries, doesn't inherit from them
	private final Map<String, ModelRegistry<M>> registries = new ConcurrentHashMap<>();
	private final Supplier<ModelRegistry<M>> registryFactory;

	/**
	 * Creates a new contextual registry with a factory for creating sub-registries.
	 *
	 * @param registryFactory Factory to create new ModelRegistry instances for each context
	 */
	public CompositeContextualModelRegistry(Supplier<ModelRegistry<M>> registryFactory) {
		this.registryFactory = registryFactory;
		// Initialize default registry
		this.registries.put(DEFAULT_CONTEXT, registryFactory.get());
	}

	/**
	 * Convenience constructor that uses BasicModelRegistry as default.
	 * Note: This creates simple registries, not contextual ones.
	 */
	public CompositeContextualModelRegistry() {
		this(BasicModelRegistry::new);
	}

	@Override
	public Optional<M> find(OpenIdentifier id) {
		return find(id, DEFAULT_CONTEXT);
	}

	@Override
	public Optional<M> find(OpenIdentifier id, String context) {
		return Optional.ofNullable(registries.get(context))
				.flatMap(registry -> registry.find(id));
	}

	@Override
	public void register(M model) {
		// Determine which context(s) to register in based on model properties
		if (model.context().isPresent() && model.target().isPresent()) {
			// Contextual model: register in specific context using target-keyed registry
			String context = model.context().get();

			// For contextual registries, use TargetKeyedModelRegistry
			ModelRegistry<M> contextRegistry = registries.computeIfAbsent(context, k -> new TargetKeyedModelRegistry<>());
			contextRegistry.register(model);
		} else {
			// Non-contextual model: register in default context
			// Use the model's ID or target ID as appropriate
			if (model.target().isPresent()) {
				// Has target but no context: register by target ID in default context
				ModelRegistry<M> targetRegistry = registries.computeIfAbsent(DEFAULT_CONTEXT + "_targets", k -> new TargetKeyedModelRegistry<>());
				targetRegistry.register(model);
			}
			// Always also register by the model's own ID
			defaultRegistry().register(model);
		}
	}

	@Override
	public ModelRegistry<M> defaultRegistry() {
		return registries.get(DEFAULT_CONTEXT);
	}

	@Override
	public Optional<ModelRegistry<M>> contextRegistry(String context) {
		return Optional.ofNullable(registries.get(context));
	}

	/**
	 * Gets all registered models across all contexts.
	 *
	 * @return A list of all models
	 */
	public java.util.List<M> allModels() {
		return registries.values().stream()
				.flatMap(registry -> registry.findAll().stream())
				.toList();
	}
}
