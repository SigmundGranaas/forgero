package com.sigmundgranaas.forgero.model.generation.impl;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.tags.engine.TagGraph;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.StructuredComponent;
import com.sigmundgranaas.forgero.model.generation.api.ModelGenerationResult;
import com.sigmundgranaas.forgero.model.generation.api.ModelGenerator;
import com.sigmundgranaas.forgero.model.generation.api.TextureGenerationTask;
import com.sigmundgranaas.forgero.model.loading.api.item.ModelTemplateProvider;
import com.sigmundgranaas.forgero.model.loading.impl.dto.ArmorModelDTO;
import com.sigmundgranaas.forgero.model.loading.impl.dto.LayerDTO;
import com.sigmundgranaas.forgero.model.loading.impl.dto.ModelDTO;
import com.sigmundgranaas.forgero.model.loading.impl.dto.TexturesDTO;
import com.sigmundgranaas.forgero.model.loading.impl.dto.templates.TemplateArmorModelDTO;
import com.sigmundgranaas.forgero.model.loading.impl.dto.templates.TemplateModelDTO;

import java.util.*;
import java.util.function.Predicate;
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
		Map<OpenIdentifier, ModelDTO> generatedItemModels = new HashMap<>();
		Map<OpenIdentifier, ArmorModelDTO> generatedArmorModels = new HashMap<>();
		List<TextureGenerationTask> textureTasks = new ArrayList<>();

		// Process Item Models
		Predicate<Component> partFilter = c -> !(c instanceof StructuredComponent sc) || sc.structure().slots().isEmpty();
		processItemTemplates(templateProvider.getPartTemplates(), components, generatedItemModels, textureTasks, partFilter);
		processItemTemplates(templateProvider.getContextualTemplates(), components, generatedItemModels, textureTasks, c -> true);
		processItemTemplates(templateProvider.getEquipmentTemplates(), components, generatedItemModels, textureTasks, c -> true);

		// Process Armor Models
		processArmorTemplates(templateProvider.getArmorTemplates(), components, generatedArmorModels, textureTasks, c -> true);

		return new ModelGenerationResult(generatedItemModels, generatedArmorModels, textureTasks);
	}

	private void processItemTemplates(Collection<? extends TemplateDataProvider<TemplateModelDTO>> templates, Map<OpenIdentifier, Component> components, Map<OpenIdentifier, ModelDTO> models, List<TextureGenerationTask> tasks, Predicate<Component> componentFilter) {
		for (TemplateDataProvider<TemplateModelDTO> template : templates) {
			List<Component> compatibleComponents = tagGraph.findTagged(template.target().tag(), components.values())
					.stream()
					.filter(componentFilter)
					.toList();

			for (Component component : compatibleComponents) {
				Map<String, Object> context = Map.of("target", component);
				for (TemplateModelDTO modelTemplate : template.models()) {
					ModelDTO resolvedModel = mapTemplateToModel(modelTemplate, context, tasks);
					models.put(resolvedModel.getOpenIdentifierId().get(), resolvedModel);
				}
			}
		}
	}

	private void processArmorTemplates(Collection<? extends TemplateDataProvider<TemplateArmorModelDTO>> templates, Map<OpenIdentifier, Component> components, Map<OpenIdentifier, ArmorModelDTO> models, List<TextureGenerationTask> tasks, Predicate<Component> componentFilter) {
		for (TemplateDataProvider<TemplateArmorModelDTO> template : templates) {
			List<Component> compatibleComponents = tagGraph.findTagged(template.target().tag(), components.values())
					.stream()
					.filter(componentFilter)
					.toList();

			for (Component component : compatibleComponents) {
				Map<String, Object> context = Map.of("target", component);
				for (TemplateArmorModelDTO modelTemplate : template.models()) {
					ArmorModelDTO resolvedModel = mapTemplateToArmorModel(modelTemplate, context, tasks);
					models.put(resolvedModel.id(), resolvedModel);
				}
			}
		}
	}

	private ArmorModelDTO mapTemplateToArmorModel(TemplateArmorModelDTO template, Map<String, Object> context, List<TextureGenerationTask> tasks) {
		String rawId = template.id() != null ? placeholderResolver.resolve(template.id(), context) : null;
		OpenIdentifier resolvedId = (rawId != null && !rawId.isEmpty()) ? new OpenIdentifier(rawId) : null;
		if (resolvedId == null) {
			throw new IllegalStateException("Generated armor model template missing 'id' field after resolution.");
		}

		String modelIdentifier = placeholderResolver.resolve(template.model(), context);
		List<LayerDTO> finalLayers = processLayerTemplates(template.layers(), context, tasks);
		String target = template.target() != null ? placeholderResolver.resolve(template.target(), context) : null;
		String modelContext = template.context() != null ? placeholderResolver.resolve(template.context(), context) : null;

		return new ArmorModelDTO(resolvedId, "forgero:armor_model", modelIdentifier, finalLayers, template.slots(), target, modelContext);
	}

	private ModelDTO mapTemplateToModel(TemplateModelDTO template, Map<String, Object> context, List<TextureGenerationTask> tasks) {
		String rawId = template.id() != null ? placeholderResolver.resolve(template.id(), context) : null;
		OpenIdentifier resolvedId = (rawId != null && !rawId.isEmpty()) ? new OpenIdentifier(rawId) : null;
		if (resolvedId == null) {
			throw new IllegalStateException("Generated model template missing explicit 'id' field after resolution. Template type: " + template.type());
		}

		// Process layers, which is now the only source of textures
		List<LayerDTO> finalLayers = processLayerTemplates(template.layers(), context, tasks);

		TexturesDTO textures = null;
		if (template.type().equals("forgero:texture_model") && !finalLayers.isEmpty()) {
			// Find the layer with the lowest order that has a texture.
			var texture = finalLayers.stream()
					.filter(layer -> layer.textures() != null && layer.textures().defaultTexture() != null)
					.min(Comparator.comparingInt(LayerDTO::order))
					.stream().findFirst();
			if (texture.isPresent()) {
				textures = texture.map(dto -> new TexturesDTO(dto.textures().defaultTexture(), dto.textures().variants())).get();
			}
		}

		String target = template.target() != null ? placeholderResolver.resolve(template.target(), context) : null;
		String modelContext = template.context() != null ? placeholderResolver.resolve(template.context(), context) : null;

		return new ModelDTO(resolvedId.toString(), template.type(), finalLayers, template.slots(), null, textures, target, modelContext, template.parent(), template.display());
	}

	private List<LayerDTO> processLayerTemplates(List<TemplateModelDTO.TemplateLayerDTO> layerTemplates, Map<String, Object> context, List<TextureGenerationTask> tasks) {
		if (layerTemplates == null) {
			return Collections.emptyList();
		}
		return layerTemplates.stream()
				.map(layerTemplate -> {
					String texture = null;
					if (layerTemplate.template() != null && layerTemplate.palette() != null && layerTemplate.output() != null) {
						String templatePath = placeholderResolver.resolve(layerTemplate.template(), context);
						String palettePath = placeholderResolver.resolve(layerTemplate.palette(), context);
						texture = placeholderResolver.resolve(layerTemplate.output(), context);
						tasks.add(new TextureGenerationTask(templatePath, palettePath, texture));
					}
					// Even if texture is null, create a TexturesDTO so LayerDTO is valid.
					// A null texture string in TexturesDTO is handled by the model rendering system.
					return new LayerDTO(layerTemplate.order(), new TexturesDTO(texture, null), null);
				})
				.collect(Collectors.toList());
	}
}
