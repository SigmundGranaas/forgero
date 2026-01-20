package com.sigmundgranaas.forgero.model.generation.impl;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.tags.api.TagResolver;
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
import com.sigmundgranaas.forgero.model.loading.impl.dto.VariantDTO;
import com.sigmundgranaas.forgero.model.loading.impl.dto.templates.TemplateArmorModelDTO;
import com.sigmundgranaas.forgero.model.loading.impl.dto.templates.TemplateModelDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ModelGeneratorImpl implements ModelGenerator {
	private static final Logger LOGGER = LoggerFactory.getLogger(ModelGeneratorImpl.class);

	private final TagResolver tagResolver;
	private final PlaceholderResolver placeholderResolver;

	public ModelGeneratorImpl(TagResolver tagResolver) {
		this.tagResolver = tagResolver;
		this.placeholderResolver = new PlaceholderResolver();
	}

	@Override
	public ModelGenerationResult generate(Map<OpenIdentifier, Component> components, ModelTemplateProvider templateProvider) {
		Map<OpenIdentifier, ModelDTO> generatedItemModels = new HashMap<>();
		Map<OpenIdentifier, ArmorModelDTO> generatedArmorModels = new HashMap<>();
		List<TextureGenerationTask> textureTasks = new ArrayList<>();

		processItemTemplates(templateProvider.getItemTemplates(), components, generatedItemModels, textureTasks, c -> true, "item");
		processItemTemplates(templateProvider.getUpgradeTemplates(), components, generatedItemModels, textureTasks, c -> true, "upgrade");
		processArmorTemplates(templateProvider.getArmorTemplates(), components, generatedArmorModels, textureTasks, c -> true);

		LOGGER.debug("Model generation: {} item models, {} armor models, {} texture tasks",
				generatedItemModels.size(), generatedArmorModels.size(), textureTasks.size());

		return new ModelGenerationResult(generatedItemModels, generatedArmorModels, textureTasks);
	}

	private void processItemTemplates(Collection<? extends TemplateDataProvider<TemplateModelDTO>> templates, Map<OpenIdentifier, Component> components, Map<OpenIdentifier, ModelDTO> models, List<TextureGenerationTask> tasks, Predicate<Component> componentFilter, String templateType) {
		for (TemplateDataProvider<TemplateModelDTO> template : templates) {
			String targetTag = template.target().tag().toString();
			List<Component> compatibleComponents = tagResolver.findTagged(template.target().tag(), components.values())
					.stream()
					.filter(componentFilter)
					.toList();

			if (compatibleComponents.isEmpty()) {
				LOGGER.warn("No components found for {} template targeting tag '{}'. " +
						"Check that components with this tag exist and are registered before model generation.",
						templateType, targetTag);
			} else {
				LOGGER.debug("Found {} components for {} template targeting tag '{}'",
						compatibleComponents.size(), templateType, targetTag);
			}

			Map<String, String> paletteMap = template.paletteMap();
			for (Component component : compatibleComponents) {
				Map<String, Component> generationContext = createGenerationContext(component);
				for (TemplateModelDTO modelTemplate : template.models()) {
					ModelDTO resolvedModel = mapTemplateToModel(modelTemplate, generationContext, tasks, paletteMap);
					models.put(resolvedModel.getOpenIdentifierId().get(), resolvedModel);
					LOGGER.debug("Generated {} model '{}' from component '{}' (context: {})",
							templateType, resolvedModel.getOpenIdentifierId().get(), component.id(),
							modelTemplate.context() != null ? modelTemplate.context() : "none");
				}
			}
		}
	}

	private void processArmorTemplates(Collection<? extends TemplateDataProvider<TemplateArmorModelDTO>> templates, Map<OpenIdentifier, Component> components, Map<OpenIdentifier, ArmorModelDTO> models, List<TextureGenerationTask> tasks, Predicate<Component> componentFilter) {
		for (TemplateDataProvider<TemplateArmorModelDTO> template : templates) {
			List<Component> compatibleComponents = tagResolver.findTagged(template.target().tag(), components.values())
					.stream()
					.filter(componentFilter)
					.toList();

			Map<String, String> paletteMap = template.paletteMap();
			for (Component component : compatibleComponents) {
				Map<String, Component> generationContext = createGenerationContext(component);
				for (TemplateArmorModelDTO modelTemplate : template.models()) {
					ArmorModelDTO resolvedModel = mapTemplateToArmorModel(modelTemplate, generationContext, tasks, paletteMap);
					models.put(resolvedModel.id(), resolvedModel);
				}
			}
		}
	}

	private Map<String, Component> createGenerationContext(Component root) {
		Map<String, Component> context = new HashMap<>();
		context.put("target", root); // The root component itself

		// Recursively unpack structured components to flatten the context
		if (root instanceof StructuredComponent structured) {
			structured.structure().allParts().forEach(slot -> {
				// Key by slot name, e.g., "head", "handle", "material"
				context.put(slot.id().name(), slot.content());
				// Also add children of children to the context
				context.putAll(createGenerationContext(slot.content()));
			});
		}
		return context;
	}

	private ArmorModelDTO mapTemplateToArmorModel(TemplateArmorModelDTO template, Map<String, Component> context, List<TextureGenerationTask> tasks, Map<String, String> paletteMap) {
		String rawId = template.id() != null ? placeholderResolver.resolve(template.id(), context, paletteMap) : null;
		OpenIdentifier resolvedId = (rawId != null && !rawId.isEmpty()) ? OpenIdentifier.parse(rawId) : null;
		if (resolvedId == null) {
			throw new IllegalStateException("Generated armor model template missing 'id' field after resolution.");
		}

		String modelIdentifier = placeholderResolver.resolve(template.model(), context, paletteMap);
		List<LayerDTO> finalLayers = processLayerTemplates(template.layers(), context, tasks, paletteMap);
		String target = template.target() != null ? placeholderResolver.resolve(template.target(), context, paletteMap) : null;
		String modelContext = template.context() != null ? placeholderResolver.resolve(template.context(), context, paletteMap) : null;

		return new ArmorModelDTO(resolvedId, "forgero:armor_model", modelIdentifier, finalLayers, template.slots(), target, modelContext);
	}

	private ModelDTO mapTemplateToModel(TemplateModelDTO template, Map<String, Component> context, List<TextureGenerationTask> tasks, Map<String, String> paletteMap) {
		String target = template.target() != null ? placeholderResolver.resolve(template.target(), context, paletteMap) : null;
		String modelContext = template.context() != null ? placeholderResolver.resolve(template.context(), context, paletteMap) : null;

		String rawId;
		if (template.id() != null) {
			rawId = placeholderResolver.resolve(template.id(), context, paletteMap);
		} else if (target != null && modelContext != null) {
			rawId = target + "-" + modelContext;
		} else if (target != null) {
			rawId = target;
		} else {
			throw new IllegalStateException("Generated model template could not resolve to a valid ID. It needs an 'id' or 'target' field. Template type: " + template.type());
		}

		OpenIdentifier resolvedId = OpenIdentifier.parse(rawId);

		List<LayerDTO> finalLayers = processLayerTemplates(template.layers(), context, tasks, paletteMap);

		TexturesDTO textures = null;
		if (template.type().equals("forgero:texture_model") && !finalLayers.isEmpty()) {
			var texture = finalLayers.stream()
					.filter(layer -> layer.textures() != null && layer.textures().defaultTexture() != null)
					.min(Comparator.comparingInt(LayerDTO::order))
					.stream().findFirst();
			if (texture.isPresent()) {
				textures = texture.map(dto -> new TexturesDTO(dto.textures().defaultTexture(), dto.textures().variants())).get();
			}
		}

		return new ModelDTO(resolvedId.toString(), template.type(), finalLayers, template.slots(), template.mountPoints(), null, textures, target, modelContext, template.parent(), template.display());
	}

	private List<LayerDTO> processLayerTemplates(List<TemplateModelDTO.TemplateLayerDTO> layerTemplates, Map<String, Component> context, List<TextureGenerationTask> tasks, Map<String, String> paletteMap) {
		if (layerTemplates == null) {
			return Collections.emptyList();
		}
		return layerTemplates.stream()
				.map(layerTemplate -> {
					String texture = null;
					
					if (layerTemplate.template() != null && layerTemplate.palette() != null && layerTemplate.output() != null) {
						String templatePath = placeholderResolver.resolve(layerTemplate.template(), context, paletteMap);
						String palettePath = placeholderResolver.resolve(layerTemplate.palette(), context, paletteMap);
						texture = placeholderResolver.resolve(layerTemplate.output(), context, paletteMap);
						tasks.add(new TextureGenerationTask(templatePath, palettePath, texture));
					}
					
					List<VariantDTO> resolvedVariants = null;
					if (layerTemplate.textures() != null) {
						if (texture == null && layerTemplate.textures().defaultTexture() != null) {
							texture = placeholderResolver.resolve(layerTemplate.textures().defaultTexture(), context, paletteMap);
						}
						
					if (layerTemplate.textures().variants() != null) {
						resolvedVariants = layerTemplate.textures().variants().stream()
								.map(templateVariant -> {
									String resolvedTexture = null;
									
									if (templateVariant.template() != null && templateVariant.palette() != null && templateVariant.output() != null) {
										String variantTemplatePath = placeholderResolver.resolve(templateVariant.template(), context, paletteMap);
										String variantPalettePath = placeholderResolver.resolve(templateVariant.palette(), context, paletteMap);
										resolvedTexture = placeholderResolver.resolve(templateVariant.output(), context, paletteMap);
										tasks.add(new TextureGenerationTask(variantTemplatePath, variantPalettePath, resolvedTexture));
									} else if (templateVariant.texture() != null) {
										resolvedTexture = placeholderResolver.resolve(templateVariant.texture(), context, paletteMap);
									}
									
									return new VariantDTO(templateVariant.predicate(), resolvedTexture, null, null);
								})
								.collect(Collectors.toList());
					}
					}
					
					return new LayerDTO(layerTemplate.order(), new TexturesDTO(texture, resolvedVariants), null);
				})
				.collect(Collectors.toList());
	}
}
