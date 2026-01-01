// FILE: forgero-core-2/src/main/java/com/sigmundgranaas/forgero/model/pipeline/api/ModelDataInitializer.java
package com.sigmundgranaas.forgero.model.pipeline.api;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.tags.api.TagResolver;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.model.api.armor.ArmorModel;
import com.sigmundgranaas.forgero.model.api.item.Model;
import com.sigmundgranaas.forgero.model.generation.api.ModelGenerationResult;
import com.sigmundgranaas.forgero.model.generation.api.ModelGenerator;
import com.sigmundgranaas.forgero.model.generation.impl.ModelGeneratorImpl;
import com.sigmundgranaas.forgero.model.loading.api.item.ModelTemplateProvider;
import com.sigmundgranaas.forgero.model.loading.impl.ModelFileLoader;
import com.sigmundgranaas.forgero.model.loading.impl.StaticModelTemplateProvider;
import com.sigmundgranaas.forgero.model.loading.impl.ModelTranslator;
import com.sigmundgranaas.forgero.model.loading.impl.dto.ArmorModelTranslator;
import com.sigmundgranaas.forgero.model.registry.api.armor.ArmorModelRegistry;
import com.sigmundgranaas.forgero.model.registry.api.item.ItemModelRegistry;
import com.sigmundgranaas.forgero.utility.resource.loader.api.ResourceProvider;

import java.util.Map;

public class ModelDataInitializer {
	private final ResourceProvider resourceProvider;

	public ModelDataInitializer(ResourceProvider resourceProvider) {
		this.resourceProvider = resourceProvider;
	}

	public ModelInitializationResult initialize(Map<OpenIdentifier, Component> components, TagResolver tagResolver, ItemModelRegistry itemModelRegistry, ArmorModelRegistry armorModelRegistry) {
		// 1. Load all files using the unified loader (with inheritance-aware tag predicates)
		ModelFileLoader fileLoader = new ModelFileLoader(resourceProvider, () -> tagResolver);
		fileLoader.load();

		// 2. Register all manually defined models immediately
		fileLoader.getManualItemModels().forEach(itemModelRegistry::register);
		fileLoader.getManualArmorModels().forEach(armorModelRegistry::register);

		// 3. Create a static template provider from the loaded templates
		ModelTemplateProvider templateProvider = new StaticModelTemplateProvider(
				fileLoader.getItemTemplates(),
				fileLoader.getUpgradeTemplates(),
				fileLoader.getArmorTemplates()
		);

		// 4. Run generator with the loaded templates
		ModelGenerator modelGenerator = new ModelGeneratorImpl(tagResolver);
		ModelGenerationResult generationResult = modelGenerator.generate(components, templateProvider);

		// 5a. Translate and register generated item models (with inheritance-aware tag predicates)
		ModelTranslator itemTranslator = new ModelTranslator(() -> tagResolver);
		generationResult.generatedModels().forEach((id, dto) -> {
			Model model = itemTranslator.toDomain(id, dto);
			itemModelRegistry.register(model);
		});

		// 5b. Translate and register generated armor models
		ArmorModelTranslator armorTranslator = new ArmorModelTranslator();
		generationResult.generatedArmorModels().forEach((id, dto) -> {
			ArmorModel armorModel = armorTranslator.toDomain(id, dto);
			armorModelRegistry.register(armorModel);
		});

		return new ModelInitializationResult(itemModelRegistry, armorModelRegistry, generationResult);
	}
}
