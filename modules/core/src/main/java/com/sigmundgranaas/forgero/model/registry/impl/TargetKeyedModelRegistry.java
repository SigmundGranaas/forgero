package com.sigmundgranaas.forgero.model.registry.impl;

import com.sigmundgranaas.forgero.common.identifier.api.Identifiable;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.model.api.Contextual;
import com.sigmundgranaas.forgero.model.registry.api.ModelRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry that keys models by their target ID instead of their own ID.
 *
 * This is used for contextual models where we want to look them up by
 * the component they target (e.g., "iron") rather than their full ID
 * (e.g., "iron-pickaxe_head_reinforcement").
 *
 * Uses composition pattern - this is a specialized registry for a specific use case.
 *
 * @param <M> The model type, must be both Identifiable and Contextual
 */
public class TargetKeyedModelRegistry<M extends Identifiable & Contextual> implements ModelRegistry<M> {
	private final Map<OpenIdentifier, M> models = new ConcurrentHashMap<>();

	@Override
	public Optional<M> find(OpenIdentifier id) {
		// For contextual models, the lookup ID is the target component ID
		return Optional.ofNullable(models.get(id));
	}

	@Override
	public void register(M model) {
		// Key by target ID if present, otherwise by model ID
		OpenIdentifier key = model.target().orElse(model.id());
		models.put(key, model);
	}

	@Override
	public List<M> findAll() {
		return new ArrayList<>(models.values());
	}
}
