// FILE: forgero-core-2/src/main/java/com/sigmundgranaas/forgero/model/registry/impl/DefaultArmorModelRegistrationService.java
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
	private static final String FORGERO_MODELS_ARMOR_DIRECTORY = "forgero/models/armor";

	public DefaultArmorModelRegistrationService(ArmorModelRegistry registry, ResourceProvider resourceProvider) {
		this.registry = registry;
		this.resourceProvider = resourceProvider;
	}

	@Override
	public void registerModels() {
		FileArmorModelProvider modelConverter = new FileArmorModelProvider(resourceProvider);
		ResourceLoader<ArmorModel> modelLoader = new ResourceLoader<>(resourceProvider, modelConverter);

		resourceProvider.getNamespaces().stream()
				.map(namespace -> new OpenIdentifier(namespace, FORGERO_MODELS_ARMOR_DIRECTORY))
				.flatMap(root -> modelLoader.load(root, true))
				.filter(model -> model.context().isEmpty() && model.target().isEmpty()) // Filter out templates
				.forEach(registry::register);
	}
}
