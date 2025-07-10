package com.sigmundgranaas.forgero.model.registry.impl;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.model.registry.api.ModelRegistrationService;
import com.sigmundgranaas.forgero.model.loading.impl.FileModelProvider;
import com.sigmundgranaas.forgero.model.registry.api.ModelRegistry;
import com.sigmundgranaas.forgero.utility.resource.loader.api.ResourceProvider;

public class DefaultModelRegistrationService implements ModelRegistrationService {
	private final ModelRegistry registry;
	private final ResourceProvider resourceProvider;
	public DefaultModelRegistrationService(ModelRegistry registry, ResourceProvider resourceProvider) {
		this.registry = registry;
		this.resourceProvider = resourceProvider;
	}
	@Override
	public void registerModels(String namespace) {
		var modelsPath = new OpenIdentifier(namespace, "models");
		var modelProvider = new FileModelProvider(resourceProvider);
		resourceProvider.list(modelsPath, true)
				.map(fileId -> {
					String relativePath = fileId.path().substring(("models/").length());
					String modelName = OpenIdentifier.normalizePathSegment(relativePath);
					return new OpenIdentifier(fileId.namespace(), modelName);
				})
				.forEach(modelId -> modelProvider.get(modelId).ifPresent(registry::register));
	}
}
