package com.sigmundgranaas.forgero.model.registry.impl;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.model.api.armor.ArmorModel;
import com.sigmundgranaas.forgero.model.loading.impl.FileArmorModelProvider;
import com.sigmundgranaas.forgero.model.registry.api.armor.ArmorModelRegistrationService;
import com.sigmundgranaas.forgero.model.registry.api.armor.ArmorModelRegistry;
import com.sigmundgranaas.forgero.utility.resource.loader.api.ResourceProvider;
import com.sigmundgranaas.forgero.utility.resource.loader.implementation.ResourceLoader;

public class DefaultArmorModelRegistrationService implements ArmorModelRegistrationService {
	private final ArmorModelRegistry registry;
	private final ResourceProvider resourceProvider;

	public DefaultArmorModelRegistrationService(ArmorModelRegistry registry, ResourceProvider resourceProvider) {
		this.registry = registry;
		this.resourceProvider = resourceProvider;
	}

	@Override
	public void registerModels(String namespace) {
		FileArmorModelProvider modelConverter = new FileArmorModelProvider(resourceProvider);
		ResourceLoader<ArmorModel> modelLoader = new ResourceLoader<>(resourceProvider, modelConverter);
		OpenIdentifier modelsRootPath = new OpenIdentifier(namespace, "forgero_models/armor");

		modelLoader.load(modelsRootPath, true)
				.forEach(registry::register);
	}
}
