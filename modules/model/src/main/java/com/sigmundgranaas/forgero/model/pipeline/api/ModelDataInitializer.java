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
import com.sigmundgranaas.forgero.model.loading.impl.ModelExtensionMerger;
import com.sigmundgranaas.forgero.model.loading.impl.ModelFileLoader;
import com.sigmundgranaas.forgero.model.loading.impl.StaticModelTemplateProvider;
import com.sigmundgranaas.forgero.model.loading.impl.ModelTranslator;
import com.sigmundgranaas.forgero.model.loading.impl.dto.ArmorModelTranslator;
import com.sigmundgranaas.forgero.model.loading.impl.dto.ModelDTO;
import com.sigmundgranaas.forgero.model.registry.api.armor.ArmorModelRegistry;
import com.sigmundgranaas.forgero.model.registry.api.item.ItemModelRegistry;
import com.sigmundgranaas.forgero.utility.resource.loader.api.ResourceProvider;

import java.util.List;
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

		// 2. Merge model extensions into manual item models
		ModelExtensionMerger merger = new ModelExtensionMerger();
		List<ModelDTO> mergedItemModels = merger.merge(
				fileLoader.getManualItemModelDTOs(),
				fileLoader.getModelExtensions()
		);

		// 3. Translate and register merged manual item models
		ModelTranslator itemTranslator = new ModelTranslator(() -> tagResolver);
		for (ModelDTO dto : mergedItemModels) {
			OpenIdentifier id = OpenIdentifier.parse(dto.id());
			Model model = itemTranslator.toDomain(id, dto);
			itemModelRegistry.register(model);
		}

		// 4. Register manual armor models (no extension support yet)
		fileLoader.getManualArmorModels().forEach(armorModelRegistry::register);

		// 5. Create a static template provider from the loaded templates
		ModelTemplateProvider templateProvider = new StaticModelTemplateProvider(
				fileLoader.getItemTemplates(),
				fileLoader.getUpgradeTemplates(),
				fileLoader.getArmorTemplates()
		);

		// 6. Run generator with the loaded templates
		ModelGenerator modelGenerator = new ModelGeneratorImpl(tagResolver);
		ModelGenerationResult generationResult = modelGenerator.generate(components, templateProvider);

		// 7a. Translate and register generated item models (reuse translator from step 3)
		generationResult.generatedModels().forEach((id, dto) -> {
			Model model = itemTranslator.toDomain(id, dto);
			itemModelRegistry.register(model);
		});

		// 7b. Translate and register generated armor models
		ArmorModelTranslator armorTranslator = new ArmorModelTranslator();
		generationResult.generatedArmorModels().forEach((id, dto) -> {
			ArmorModel armorModel = armorTranslator.toDomain(id, dto);
			armorModelRegistry.register(armorModel);
		});

		return new ModelInitializationResult(itemModelRegistry, armorModelRegistry, generationResult);
	}
}
