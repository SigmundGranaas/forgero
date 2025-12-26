package com.sigmundgranaas.forgero.model.registry.impl;

import com.sigmundgranaas.forgero.common.identifier.api.Identifiable;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.model.registry.api.ModelRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Basic generic model registry implementation using pure composition.
 *
 * This is a simple, reusable implementation that works with ANY Identifiable model type.
 * It's used as a building block for more complex registries through composition.
 *
 * @param <M> The model type, must be Identifiable
 */
public class BasicModelRegistry<M extends Identifiable> implements ModelRegistry<M> {
	private final Map<OpenIdentifier, M> models = new ConcurrentHashMap<>();

	@Override
	public Optional<M> find(OpenIdentifier id) {
		return Optional.ofNullable(models.get(id));
	}

	@Override
	public void register(M model) {
		models.put(model.id(), model);
	}

	@Override
	public List<M> findAll() {
		return new ArrayList<>(models.values());
	}
}
