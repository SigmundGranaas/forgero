package com.sigmundgranaas.forgero.model.api;

import com.sigmundgranaas.forgero.core.component.api.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Holds all the contextual information needed during the model resolution process.
 * This includes the root component, the current component, any context provided by a parent,
 * and dynamic state like bow pull animation.
 *
 * @param root           The root component of the entire assembly (e.g., the complete tool).
 * @param aComponent     The specific component currently being resolved.
 * @param parentContext  An optional context string provided by a parent slot.
 * @param dynamicState   A map for dynamic, stateful properties like animation frames.
 */
public record ModelResolutionContext(
		Component root,
		Component aComponent,
		Optional<String> parentContext,
		Map<String, Object> dynamicState
) {

	public ModelResolutionContext(Component root, Component aComponent) {
		this(root, aComponent, Optional.empty(), new HashMap<>());
	}

	public Optional<Object> get(String key) {
		return Optional.ofNullable(dynamicState.get(key));
	}

	public ModelResolutionContext with(Component component) {
		return new ModelResolutionContext(this.root, component, Optional.empty(), this.dynamicState);
	}

	public ModelResolutionContext with(Component component, String context) {
		return new ModelResolutionContext(this.root, component, Optional.ofNullable(context), this.dynamicState);
	}
}
