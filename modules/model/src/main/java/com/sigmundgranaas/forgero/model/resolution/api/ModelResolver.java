package com.sigmundgranaas.forgero.model.resolution.api;

import com.sigmundgranaas.forgero.common.identifier.api.Identifiable;
import com.sigmundgranaas.forgero.core.component.api.Component;

import java.util.List;

/**
 * Generic interface for resolving components to models.
 * Uses composition to work with any Identifiable model type.
 *
 * Model resolvers are responsible for converting a component (which may be composite)
 * into one or more models for rendering. This allows the rendering system to be
 * decoupled from the component system.
 *
 * @param <M> The model type, must be Identifiable
 */
@FunctionalInterface
public interface ModelResolver<M extends Identifiable> {
	/**
	 * Resolves a component into a list of models.
	 *
	 * For simple components, this typically returns a single model.
	 * For composite components (e.g., tools with multiple parts), this may
	 * return multiple models that should all be rendered.
	 *
	 * @param component The component to resolve
	 * @return A list of models for this component, may be empty if no models are found
	 */
	List<M> resolve(Component component);
}
