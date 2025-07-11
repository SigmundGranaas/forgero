package com.sigmundgranaas.forgero.model.generation.impl;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.tags.engine.TagGraph;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.StructuredComponent;
import com.sigmundgranaas.forgero.model.generation.api.ModelGenerationResult;
import com.sigmundgranaas.forgero.model.generation.api.ModelGenerator;
import com.sigmundgranaas.forgero.model.generation.api.TextureGenerationTask;
import com.sigmundgranaas.forgero.model.loading.api.ModelTemplateProvider;
import com.sigmundgranaas.forgero.model.loading.impl.dto.LayerDTO;
import com.sigmundgranaas.forgero.model.loading.impl.dto.ModelDTO;
import com.sigmundgranaas.forgero.model.loading.impl.dto.TexturesDTO;
import com.sigmundgranaas.forgero.model.loading.impl.dto.templates.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ModelGeneratorImpl implements ModelGenerator {

	private final TagGraph tagGraph;
	private final PlaceholderResolver placeholderResolver;

	public ModelGeneratorImpl(TagGraph tagGraph) {
		this.tagGraph = tagGraph;
		this.placeholderResolver = new PlaceholderResolver();
	}

	@Override
	public ModelGenerationResult generate(Map<OpenIdentifier, Component> components, ModelTemplateProvider templateProvider) {
		Map<OpenIdentifier, ModelDTO> generatedModels = new HashMap<>();
		List<TextureGenerationTask> textureTasks = new ArrayList<>();

		// Handle Part templates with new ID generation logic
		templateProvider.getPartTemplates().forEach((partName, template) -> {
			List<Component> compatibleComponents = tagGraph.findTagged(template.target().tag(), components.values())
					.stream()
					// Only apply part templates to base materials/parts, not existing composite parts.
					// This heuristic checks if a component has a complex structure.
					.filter(c -> !(c instanceof StructuredComponent sc) || sc.structure().slots().isEmpty())
					.toList();

			for (Component component : compatibleComponents) {
				Map<String, Object> context = Map.of("target", component);
				ModelDTO resolvedModel = mapTemplateToModel(template.model(), context, textureTasks);

				// Generate the new ID from material name and part name (from the template filename)
				String newPath = component.id().path() + "-" + partName;
				OpenIdentifier newModelId = new OpenIdentifier(component.id().namespace(), newPath);

				generatedModels.put(newModelId, resolvedModel);
			}
		});

		// Handle Contextual and Equipment templates as before
		processTemplates(templateProvider.getContextualTemplates(), components, generatedModels, textureTasks);
		processTemplates(templateProvider.getEquipmentTemplates(), components, generatedModels, textureTasks);


		return new ModelGenerationResult(generatedModels, textureTasks);
	}

	private void processTemplates(Collection<? extends TemplateModelDataProvider> templates, Map<OpenIdentifier, Component> components, Map<OpenIdentifier, ModelDTO> models, List<TextureGenerationTask> tasks) {
		for (TemplateModelDataProvider template : templates) {
			// Find all components that match the template's target tag
			List<Component> compatibleComponents = tagGraph.findTagged(template.target().tag(), components.values());

			for (Component component : compatibleComponents) {
				Map<String, Object> context = Map.of("target", component);
				ModelDTO resolvedModel = mapTemplateToModel(template.model(), context, tasks);

				// Use the resolved model's target and context for registration for contextual models,
				// or the component's ID for equipment models.
				if (resolvedModel.getTarget().isPresent() && resolvedModel.getContext().isPresent()) {
					String uniqueKey = "contextual-" + component.id().toString() + "-" + resolvedModel.getContext().get();
					models.put(new OpenIdentifier(uniqueKey), resolvedModel);
				} else {
					models.put(component.id(), resolvedModel);
				}
			}
		}
	}

	private ModelDTO mapTemplateToModel(TemplateModelDTO template, Map<String, Object> context, List<TextureGenerationTask> tasks) {
		List<LayerDTO> finalLayers = null;
		if (template.layers() != null) {
			finalLayers = template.layers().stream()
					.map(layerTemplate -> {
						var generation = layerTemplate.textures().generation();
						String texture = processGenerationBlock(generation, context, tasks);
						return new LayerDTO(layerTemplate.order(), new TexturesDTO(texture, null), null);
					})
					.collect(Collectors.toList());
		}

		TexturesDTO finalTextures = null;
		if (template.textures() != null && template.textures().generation() != null) {
			String texture = processGenerationBlock(template.textures().generation(), context, tasks);
			finalTextures = new TexturesDTO(texture, null);
		}

		String target = template.target() != null ? placeholderResolver.resolve(template.target(), context) : null;
		String modelContext = template.context() != null ? placeholderResolver.resolve(template.context(), context) : null;

		return new ModelDTO(template.type(), finalLayers, template.slots(), null, finalTextures, target, modelContext);
	}

	private String processGenerationBlock(GenerationDTO generation, Map<String, Object> context, List<TextureGenerationTask> tasks) {
		String templatePath = placeholderResolver.resolve(generation.template(), context);
		String palettePath = placeholderResolver.resolve(generation.palette(), context);
		String outputPath = placeholderResolver.resolve(generation.output(), context);

		tasks.add(new TextureGenerationTask(templatePath, palettePath, outputPath));
		return outputPath;
	}


	/**
	 * A common interface for all template DTOs that provide a model for a target.
	 * This simplifies the generation logic by providing a uniform way to access
	 * the essential parts of a template.
	 */
	public interface TemplateModelDataProvider {
		TargetDTO target();
		TemplateModelDTO model();
	}
}
