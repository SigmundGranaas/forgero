package com.sigmundgranaas.forgero.model.util;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Component;

import java.util.Optional;

/**
 * Utility to create model identifiers, potentially modified by a context.
 * This class encapsulates the simple, predictable logic for combining a component's base ID
 * with a context provided by a parent slot.
 */
public class ModelIdentifierFactory {

	/**
	 * Creates a model identifier for a component. If a context is provided, it attempts
	 * to create a new, context-specific identifier. Otherwise, it returns the component's own ID.
	 *
	 * @param component The component for which to create the model ID.
	 * @param context   An optional context string provided by a parent slot.
	 * @return The resolved model ID.
	 */
	public OpenIdentifier create(Component component, Optional<String> context) {
		return context
				.map(ctx -> fromContext(component, ctx))
				.orElse(component.id());
	}

	/**
	 * Creates a new ID by prepending "upgrades/" and appending the context string as a suffix
	 * to the component's ID path.
	 * Example:
	 * - Component ID: "forgero:iron"
	 * - Context: "pickaxe_head_reinforcement"
	 * - Resulting ID: "forgero:upgrades/iron-pickaxe_head_reinforcement"
	 *
	 * @param component The component providing the base ID.
	 * @param context   The suffix to append.
	 * @return A new, combined OpenIdentifier.
	 */
	private OpenIdentifier fromContext(Component component, String context) {
		String newPath = "upgrades/" + component.id().path() + "-" + context;
		return new OpenIdentifier(component.id().namespace(), newPath);
	}
}
