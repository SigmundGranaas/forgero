package com.sigmundgranaas.forgero.model.registry.api.item;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.model.api.item.Model;

import java.util.List;
import java.util.Optional;

public interface ItemModelRegistry {
	/**
	 * Finds a model in the default context.
	 *
	 * @param id The identifier of the model.
	 * @return An optional containing the model if found.
	 */
	Optional<Model> find(OpenIdentifier id);

	/**
	 * Finds a model for a given component ID within a specific context.
	 *
	 * @param id The identifier of the target component (e.g., forgero:iron).
	 * @param context The context string (e.g., "pickaxe_head_reinforcement").
	 * @return An optional containing the contextual model if found.
	 */
	Optional<Model> find(OpenIdentifier id, String context);

	void register(Model model);

	List<Model> models();
}
