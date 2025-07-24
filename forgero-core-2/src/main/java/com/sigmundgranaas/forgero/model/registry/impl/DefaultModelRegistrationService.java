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

	public DefaultModelRegistrationService(ItemModelRegistry registry, ResourceProvider resourceProvider) {
		this.registry = registry;
		this.resourceProvider = resourceProvider;
	}

	@Override
	public void registerModels(String namespace) {
		// Create the FileModelProvider which now acts as a ResourceConverter<Model>
		FileModelProvider modelConverter = new FileModelProvider(resourceProvider);

		// Create a generic ResourceLoader specifically for Model types
		ResourceLoader<Model> modelLoader = new ResourceLoader<>(resourceProvider, modelConverter);

		// Define the root path for models within the namespace's assets
		OpenIdentifier modelsRootPath = new OpenIdentifier(namespace, "models");

		// Load and register all models
		// The `modelLoader.load` method will return a stream of Model objects,
		// and each Model object will already have its normalized OpenIdentifier
		// thanks to the changes in FileModelProvider.convert().
		// Simply register the model. Its ID is already correct.
		// Corrected: only pass the Model object
		modelLoader.load(modelsRootPath, true) // Recursively load all models
				.forEach(registry::register);
	}
}
