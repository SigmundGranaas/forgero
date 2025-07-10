package com.sigmundgranaas.forgero.model.registry.impl;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.model.api.Model;
import com.sigmundgranaas.forgero.model.registry.api.ModelRegistry;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class MapBackedModelRegistry implements ModelRegistry {

	private final Map<OpenIdentifier, Model> models = new ConcurrentHashMap<>();
	@Override public Optional<Model> find(OpenIdentifier id) { return Optional.ofNullable(models.get(id)); }
	@Override public void register(Model model) { models.put(model.getIdentifier(), model); }
}
