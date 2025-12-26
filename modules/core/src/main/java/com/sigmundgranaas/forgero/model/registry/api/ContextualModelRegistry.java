package com.sigmundgranaas.forgero.model.registry.api;

import com.sigmundgranaas.forgero.common.identifier.api.Identifiable;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.model.api.Contextual;

import java.util.Optional;

/**
 * Registry for contextual models using pure composition.
 *
 * This registry handles models that can be registered in different contexts
 * (e.g., a trim model that applies differently to helmets vs chestplates).
 *
 * Uses composition pattern: CONTAINS multiple ModelRegistry instances
 * rather than extending a base class.
 *
 * @param <M> The model type, must be both Identifiable and Contextual
 */
public interface ContextualModelRegistry<M extends Identifiable & Contextual> {
	/**
	 * Finds a model in the default context.
	 *
	 * @param id The identifier of the model
	 * @return An optional containing the model if found
	 */
	Optional<M> find(OpenIdentifier id);

	/**
	 * Finds a model for a given component ID within a specific context.
	 *
	 * @param id      The identifier of the target component (e.g., forgero:iron)
	 * @param context The context string (e.g., "pickaxe_head_reinforcement")
	 * @return An optional containing the contextual model if found
	 */
	Optional<M> find(OpenIdentifier id, String context);

	/**
	 * Registers a model. The model will be registered in the appropriate
	 * context based on its target and context properties.
	 *
	 * @param model The model to register
	 */
	void register(M model);

	/**
	 * Gets the registry for the default context.
	 * This allows access to the underlying basic registry for simple operations.
	 *
	 * @return The default context registry
	 */
	ModelRegistry<M> defaultRegistry();

	/**
	 * Gets the registry for a specific context.
	 *
	 * @param context The context name
	 * @return An optional containing the context registry if it exists
	 */
	Optional<ModelRegistry<M>> contextRegistry(String context);
}
