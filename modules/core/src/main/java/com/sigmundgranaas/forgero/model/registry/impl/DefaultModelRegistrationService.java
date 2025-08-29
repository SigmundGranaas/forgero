package com.sigmundgranaas.forgero.model.registry.impl;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.model.api.item.Model;
import com.sigmundgranaas.forgero.model.loading.impl.FileModelProvider;
import com.sigmundgranaas.forgero.model.registry.api.item.ItemModelRegistrationService;
import com.sigmundgranaas.forgero.model.registry.api.item.ItemModelRegistry;
import com.sigmundgranaas.forgero.utility.resource.loader.api.ResourceProvider;
import com.sigmundgranaas.forgero.utility.resource.loader.implementation.ResourceLoader;

public class DefaultModelRegistrationService implements ItemModelRegistrationService {
	private final ItemModelRegistry registry;
	private final ResourceProvider resourceProvider;
	private static final String FORGERO_MODELS_DIRECTORY = "forgero/models";

	public DefaultModelRegistrationService(ItemModelRegistry registry, ResourceProvider resourceProvider) {
		this.registry = registry;
		this.resourceProvider = resourceProvider;
	}

	@Override
	public void registerModels() {
		FileModelProvider modelConverter = new FileModelProvider();
		ResourceLoader<Model> modelLoader = new ResourceLoader<>(resourceProvider, modelConverter);

		resourceProvider.getNamespaces().stream()
				.map(namespace -> new OpenIdentifier(namespace, FORGERO_MODELS_DIRECTORY))
				.flatMap(root -> modelLoader.load(root, true))
				.filter(model -> model.getContext().isEmpty()) // Filter out templates
				.forEach(registry::register);
	}
}
