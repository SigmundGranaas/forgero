package com.sigmundgranaas.forgero.model.api;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;

import java.util.Optional;

/**
 * Capability interface for models that are contextual - meaning they can target
 * specific components and be registered for specific contexts.
 *
 * This is primarily used for template-based model generation where a single
 * model definition can be applied to multiple components.
 */
public interface Contextual {
	/**
	 * @return The target component ID this model is designed for (used for contextual registration).
	 */
	Optional<OpenIdentifier> target();

	/**
	 * @return The context string this model belongs to (e.g., "helmet_trim", "pickaxe_head_reinforcement").
	 */
	Optional<String> context();
}
