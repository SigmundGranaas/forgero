package com.sigmundgranaas.forgero.model.registry.impl;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.model.api.item.Model;
import com.sigmundgranaas.forgero.model.registry.api.ModelRegistry;
import com.sigmundgranaas.forgero.model.registry.api.item.ItemModelRegistry;

import java.util.List;
import java.util.Optional;

/**
 * Item model registry implementation using pure composition.
 *
 * This class COMPOSES a CompositeContextualModelRegistry rather than
 * implementing the logic itself. This demonstrates the composition pattern
 * where behavior is delegated to contained objects.
 */
public class MapBackedModelRegistry implements ItemModelRegistry {
	// Composition: CONTAINS a contextual registry, delegates all work to it
	private final CompositeContextualModelRegistry<Model> delegate;

	public MapBackedModelRegistry() {
		this.delegate = new CompositeContextualModelRegistry<>();
	}

	@Override
	public Optional<Model> find(OpenIdentifier id) {
		return delegate.find(id);
	}

	@Override
	public Optional<Model> find(OpenIdentifier id, String context) {
		return delegate.find(id, context);
	}

	@Override
	public void register(Model model) {
		delegate.register(model);
	}

	@Override
	public ModelRegistry<Model> defaultRegistry() {
		return delegate.defaultRegistry();
	}

	@Override
	public Optional<ModelRegistry<Model>> contextRegistry(String context) {
		return delegate.contextRegistry(context);
	}

	@Override
	public List<Model> models() {
		return delegate.allModels();
	}
}

