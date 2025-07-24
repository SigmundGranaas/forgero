package com.sigmundgranaas.forgero.model.pipeline.api;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.tags.engine.TagGraph;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.model.api.armor.ArmorModel;
import com.sigmundgranaas.forgero.model.api.item.Model;
import com.sigmundgranaas.forgero.model.generation.api.ModelGenerationResult;
import com.sigmundgranaas.forgero.model.generation.api.ModelGenerator;
import com.sigmundgranaas.forgero.model.generation.impl.ModelGeneratorImpl;
import com.sigmundgranaas.forgero.model.loading.api.item.ModelTemplateProvider;
import com.sigmundgranaas.forgero.model.loading.impl.FileModelTemplateProvider;
import com.sigmundgranaas.forgero.model.loading.impl.ModelTranslator;
import com.sigmundgranaas.forgero.model.loading.impl.dto.ArmorModelTranslator;
import com.sigmundgranaas.forgero.model.registry.api.armor.ArmorModelRegistry;
import com.sigmundgranaas.forgero.model.registry.api.item.ItemModelRegistry;
import com.sigmundgranaas.forgero.utility.resource.loader.api.ResourceProvider;

import java.util.Map;

/**
 * Orchestrates the entire model data initialization process.
 * 1. Loads all model templates (item, armor, etc.).
 * 2. Runs the generation logic to produce model DTOs and texture tasks.
 * 3. Translates the generated DTOs into domain models and populates the provided registries.
 */
public class ModelDataInitializer {
	private final ResourceProvider resourceProvider;
	private final String namespace;

	public ModelDataInitializer(ResourceProvider resourceProvider, String namespace) {
		this.resourceProvider = resourceProvider;
		this.namespace = namespace;
	}

	public ModelInitializationResult initialize(Map<OpenIdentifier, Component> components, TagGraph tagGraph, ItemModelRegistry itemModelRegistry, ArmorModelRegistry armorModelRegistry) {
		// 1. Load templates for all model types
		ModelTemplateProvider templateProvider = new FileModelTemplateProvider(resourceProvider, namespace);

		// 2. Run generator with final components
		ModelGenerator modelGenerator = new ModelGeneratorImpl(tagGraph);
		ModelGenerationResult generationResult = modelGenerator.generate(components, templateProvider);

		// 3a. Translate and register generated item models
		ModelTranslator itemTranslator = new ModelTranslator();
		generationResult.generatedModels().forEach((id, dto) -> {
			Model model = itemTranslator.toDomain(id, dto);
			itemModelRegistry.register(model);
		});

		// 3b. Translate and register generated armor models
		ArmorModelTranslator armorTranslator = new ArmorModelTranslator();
		generationResult.generatedArmorModels().forEach((id, dto) -> {
			ArmorModel armorModel = armorTranslator.toDomain(id, dto);
			armorModelRegistry.register(armorModel);
		});

		// 4. Return the populated registries and the generation results for optional processing (like file writing)
		return new ModelInitializationResult(itemModelRegistry, armorModelRegistry, generationResult);
	}
}
