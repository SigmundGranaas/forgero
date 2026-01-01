package com.sigmundgranaas.forgero.model.registry.api;

import com.sigmundgranaas.forgero.common.identifier.api.Identifiable;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;

import java.util.List;
import java.util.Optional;

/**
 * Generic registry for models.
 * Uses composition to work with any model type that is Identifiable.
 *
 * This provides a simple, unified interface for model registries.
 * For contextual model support (e.g., models that change based on context),
 * use ContextualModelRegistry instead.
 *
 * @param <M> The model type, must be Identifiable
 */
public interface ModelRegistry<M extends Identifiable> {
	/**
	 * Finds a model by its identifier.
	 *
	 * @param id The model identifier
	 * @return An optional containing the model if found
	 */
	Optional<M> find(OpenIdentifier id);

	/**
	 * Registers a model in the registry.
	 *
	 * @param model The model to register
	 */
	void register(M model);

	/**
	 * Gets all registered models.
	 *
	 * @return A list of all registered models
	 */
	List<M> findAll();
}
