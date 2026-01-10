package com.sigmundgranaas.forgero.model.resolution.api.item;

import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.model.api.RenderableTexture;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ItemModelResolver {
	/**
	 * Resolves a component to a list of renderable textures with default (empty) dynamic state.
	 */
	default Optional<List<RenderableTexture>> resolve(Component component) {
		return resolve(component, Collections.emptyMap());
	}

	/**
	 * Resolves a component to a list of renderable textures with dynamic state.
	 * Dynamic state includes runtime information like bow pull progress.
	 *
	 * @param component    The component to resolve
	 * @param dynamicState Dynamic state map (e.g., "pulling" -> true, "pull" -> 0.65f)
	 * @return Sorted list of renderable textures
	 */
	Optional<List<RenderableTexture>> resolve(Component component, Map<String, Object> dynamicState);
}
