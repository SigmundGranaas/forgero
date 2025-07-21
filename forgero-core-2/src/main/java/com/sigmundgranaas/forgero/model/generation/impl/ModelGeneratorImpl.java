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

		// This part can be removed if you make PartModelTemplateDTO also use a list
		templateProvider.getPartTemplates().forEach((partName, template) -> {
			List<Component> compatibleComponents = tagGraph.findTagged(template.target().tag(), components.values())
					.stream()
					.filter(c -> !(c instanceof StructuredComponent sc) || sc.structure().slots().isEmpty())
					.toList();

			for (Component component : compatibleComponents) {
				Map<String, Object> context = Map.of("target", component);
				// Assuming PartModelTemplateDTO is also updated to provide a list
				for (TemplateModelDTO modelTemplate : template.models()) {
					ModelDTO resolvedModel = mapTemplateToModel(modelTemplate, context, textureTasks);
					generatedModels.put(resolvedModel.id(), resolvedModel);
				}
			}
		});

		processTemplates(templateProvider.getContextualTemplates(), components, generatedModels, textureTasks);
		processTemplates(templateProvider.getEquipmentTemplates(), components, generatedModels, textureTasks);

		// This custom loop is no longer needed as the generic one handles it.
		// We just need to make sure we don't load MultiModelTemplateDTOs anymore.
		// The loading logic in FileModelTemplateProvider should be adjusted to parse all templates
		// using their respective codecs, which now all support lists.

		return new ModelGenerationResult(generatedModels, textureTasks);
	}

	private void processTemplates(Collection<? extends TemplateModelDataProvider> templates, Map<OpenIdentifier, Component> components, Map<OpenIdentifier, ModelDTO> models, List<TextureGenerationTask> tasks) {
		for (TemplateModelDataProvider template : templates) {
			// Find all components that match the template's target tag
			List<Component> compatibleComponents = tagGraph.findTagged(template.target().tag(), components.values());

			for (Component component : compatibleComponents) {
				Map<String, Object> context = Map.of("target", component);
				// CHANGE: Loop over the list of models from the template
				for(TemplateModelDTO modelTemplate : template.models()) {
					ModelDTO resolvedModel = mapTemplateToModel(modelTemplate, context, tasks);
					models.put(resolvedModel.id(), resolvedModel);
				}
			}
		}
	}

	private ModelDTO mapTemplateToModel(TemplateModelDTO template, Map<String, Object> context, List<TextureGenerationTask> tasks) {
		String rawId = template.id() != null ? placeholderResolver.resolve(template.id(), context) : null;
		OpenIdentifier resolvedId = null;
		if (rawId != null && !rawId.isEmpty()) {
			resolvedId = new OpenIdentifier(rawId);
		}

		if (resolvedId == null) {
			throw new IllegalStateException("Generated model template missing explicit 'id' field after resolution. Template: " + template.type());
		}

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

		return new ModelDTO(resolvedId, template.type(), finalLayers, template.slots(), null, finalTextures, target, modelContext, template.parent(), template.display());
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
		List<TemplateModelDTO> models();
	}
}
