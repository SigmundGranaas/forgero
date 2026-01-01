package com.sigmundgranaas.forgero.model.registry.api.item;

import com.sigmundgranaas.forgero.model.api.item.Model;
import com.sigmundgranaas.forgero.model.registry.api.ContextualModelRegistry;

/**
 * Registry for item models using pure composition.
 *
 * This interface extends ContextualModelRegistry, demonstrating composition
 * of capabilities. Item models support contextual registration (e.g., different
 * textures for the same material in different contexts).
 *
 * Note: This extends an interface (contract composition), not a class.
 * The implementation uses composition internally.
 */
public interface ItemModelRegistry extends ContextualModelRegistry<Model> {
	/**
	 * Gets all registered models across all contexts.
	 * This is a convenience method specific to item models.
	 *
	 * @return A list of all models
	 */
	default java.util.List<Model> models() {
		return defaultRegistry().findAll();
	}
}

