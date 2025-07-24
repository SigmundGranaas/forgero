package com.sigmundgranaas.forgero.model.registry.impl;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.model.api.item.Model;
import com.sigmundgranaas.forgero.model.registry.api.item.ItemModelRegistry;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class MapBackedModelRegistry implements ItemModelRegistry {
	private static final String DEFAULT_CONTEXT = "default";
	private final Map<String, Map<OpenIdentifier, Model>> models = new ConcurrentHashMap<>();

	@Override
	public void register(Model model) {
		// If a model has both a context and a target, register it in the corresponding contextual map.
		// Otherwise, register it in the default map using its own file-based identifier.
		if (model.getContext().isPresent() && model.getTarget().isPresent()) {
			String context = model.getContext().get();
			OpenIdentifier targetId = model.getTarget().get();
			models.computeIfAbsent(context, k -> new ConcurrentHashMap<>()).put(targetId, model);
		} else {
			models.computeIfAbsent(DEFAULT_CONTEXT, k -> new ConcurrentHashMap<>()).put(model.getIdentifier(), model);
		}
	}

	@Override
	public Optional<Model> find(OpenIdentifier id) {
		return find(id, DEFAULT_CONTEXT);
	}

	@Override
	public Optional<Model> find(OpenIdentifier id, String context) {
		return Optional.ofNullable(models.get(context))
				.flatMap(contextMap -> Optional.ofNullable(contextMap.get(id)));
	}

	@Override
	public List<Model> models() {
		return models.values().stream()
				.flatMap(map -> map.values().stream())
				.toList();
	}
}
