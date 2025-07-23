package com.sigmundgranaas.forgero.model.pipeline.api;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.tags.engine.TagGraph;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.model.api.item.Model;
import com.sigmundgranaas.forgero.model.generation.api.ModelGenerationResult;
import com.sigmundgranaas.forgero.model.generation.api.item.ItemModelGenerator;
import com.sigmundgranaas.forgero.model.generation.impl.ModelGeneratorImpl;
import com.sigmundgranaas.forgero.model.loading.api.item.ItemModelTemplateProvider;
import com.sigmundgranaas.forgero.model.loading.impl.FileModelTemplateProvider;
import com.sigmundgranaas.forgero.model.loading.impl.ModelTranslator;
import com.sigmundgranaas.forgero.model.registry.api.item.ItemModelRegistry;
import com.sigmundgranaas.forgero.model.registry.impl.DefaultModelRegistrationService;
import com.sigmundgranaas.forgero.utility.resource.loader.api.ResourceProvider;

import java.util.Map;

public class ModelDataInitializer {
	private final ResourceProvider resourceProvider;
	private final String namespace;

	public ModelDataInitializer(ResourceProvider resourceProvider, String namespace) {
		this.resourceProvider = resourceProvider;
		this.namespace = namespace;
	}

	public ModelInitializationResult initialize(Map<OpenIdentifier, Component> components, TagGraph tagGraph, ItemModelRegistry modelRegistry) {
		// 1. Load templates
		ItemModelTemplateProvider templateProvider = new FileModelTemplateProvider(resourceProvider, namespace);

		// 2. Run generator with final components
		ItemModelGenerator modelGenerator = new ModelGeneratorImpl(tagGraph);
		ModelGenerationResult generationResult = modelGenerator.generate(components, templateProvider);

		// 3. Translate and register generated models
		ModelTranslator translator = new ModelTranslator();
		generationResult.generatedModels().forEach((id, dto) -> {
			Model model = translator.toDomain(id, dto);
			modelRegistry.register(model);
		});

		// 4. Load and register manual overrides
		DefaultModelRegistrationService manualModelService = new DefaultModelRegistrationService(modelRegistry, resourceProvider);
		//manualModelService.registerModels(namespace);

		// 5. Return the populated registry and the generation results for optional processing (like file writing)
		return new ModelInitializationResult(modelRegistry, generationResult);
	}
}
