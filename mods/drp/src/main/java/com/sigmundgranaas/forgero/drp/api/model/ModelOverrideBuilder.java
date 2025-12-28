package com.sigmundgranaas.forgero.drp.api.model;

import com.sigmundgranaas.forgero.drp.impl.builder.ModelOverrideBuilderImpl;

import java.util.Map;

/**
 * Builder for model overrides (predicate-based model switching).
 */
public interface ModelOverrideBuilder {

	/**
	 * Creates a new model override builder.
	 *
	 * @return A new builder instance
	 */
	static ModelOverrideBuilder create() {
		return new ModelOverrideBuilderImpl();
	}

	/**
	 * Adds a custom model data predicate.
	 *
	 * @param value The custom model data value
	 * @return This builder for chaining
	 */
	ModelOverrideBuilder customModelData(int value);

	/**
	 * Adds a damage predicate.
	 *
	 * @param damage The damage value (0.0 to 1.0)
	 * @return This builder for chaining
	 */
	ModelOverrideBuilder damage(float damage);

	/**
	 * Adds a damaged predicate (whether item is damaged at all).
	 *
	 * @param damaged Whether the item should be damaged
	 * @return This builder for chaining
	 */
	ModelOverrideBuilder damaged(boolean damaged);

	/**
	 * Adds a pulling predicate (for bows).
	 *
	 * @param pulling The pulling value
	 * @return This builder for chaining
	 */
	ModelOverrideBuilder pulling(float pulling);

	/**
	 * Adds a pull predicate (bow draw progress).
	 *
	 * @param pull The pull value (0.0 to 1.0)
	 * @return This builder for chaining
	 */
	ModelOverrideBuilder pull(float pull);

	/**
	 * Adds a custom predicate.
	 *
	 * @param predicateId The predicate identifier
	 * @param value       The predicate value
	 * @return This builder for chaining
	 */
	ModelOverrideBuilder predicate(String predicateId, float value);

	/**
	 * Sets the model to use when predicates match.
	 *
	 * @param modelId The model identifier
	 * @return This builder for chaining
	 */
	ModelOverrideBuilder model(String modelId);

	/**
	 * Gets the predicates.
	 *
	 * @return Map of predicate ID to value
	 */
	Map<String, Float> getPredicates();

	/**
	 * Gets the model identifier.
	 *
	 * @return The model ID
	 */
	String getModel();
}
