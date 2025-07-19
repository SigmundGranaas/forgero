package com.sigmundgranaas.forgero.model.resolution.impl;

import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.StructuredComponent;
import com.sigmundgranaas.forgero.core.component.api.structure.StructureSlot;
import com.sigmundgranaas.forgero.model.api.*;
import com.sigmundgranaas.forgero.model.registry.api.ModelRegistry;
import com.sigmundgranaas.forgero.model.resolution.api.ModelResolver;

import java.util.*;
import java.util.stream.Collectors;

public class RecursiveModelResolver implements ModelResolver {
	private final ModelRegistry modelRegistry;

	public RecursiveModelResolver(ModelRegistry modelRegistry) {
		this.modelRegistry = modelRegistry;
	}

	@Override
	public Optional<List<RenderableTexture>> resolve(Component component) {
		List<RenderableTexture> renderableTextures = resolveComponent(component, new ModelResolutionContext(component, component));
		if (renderableTextures.isEmpty()) {
			return Optional.empty();
		}
		List<RenderableTexture> sortedTextures = renderableTextures.stream()
				.sorted()
				.collect(Collectors.toList());

		return Optional.of(sortedTextures);
	}

	private List<RenderableTexture> resolveComponent(Component component, ModelResolutionContext context) {
		Optional<Model> modelOpt = modelRegistry.find(component.id());
		return modelOpt.map(model -> collectTexturesFromKnownModel(model, component, 0, context))
				.orElse(Collections.emptyList());
	}

	private List<RenderableTexture> collectTexturesFromKnownModel(Model model, Component component, int baseOrder, ModelResolutionContext context) {
		Model resolvedModel = model.apply(context);
		if (resolvedModel instanceof EmptyModel) {
			return Collections.emptyList();
		}

		if (resolvedModel instanceof CompositeModel composite) {
			return getTexturesFromComposite(composite, component, context, baseOrder);
		} else if (resolvedModel instanceof TextureModel textureModel) {
			return getTexturesFromTextureModel(textureModel, context, baseOrder);
		}
		return Collections.emptyList();
	}

	private List<RenderableTexture> getTexturesFromComposite(CompositeModel composite, Component component, ModelResolutionContext context, int baseOrder) {
		List<RenderableTexture> textures = new ArrayList<>();
		for (ModelLayer layer : composite.layers()) {
			textures.add(getLayerTexture(layer, context, baseOrder));
		}

		if (component instanceof StructuredComponent structured) {
			// Create a map of the component's actual children, keyed by their slot's path.
			Map<String, Component> filledSlots = structured.structure().slots().values().stream()
					.collect(Collectors.toMap(slot -> slot.id().path(), StructureSlot::content));

			for (ModelSlot modelSlot : composite.slots()) {
				Component childComponent = filledSlots.get(modelSlot.id());
				if (childComponent != null) {
					textures.addAll(resolveSlot(modelSlot, childComponent, context, baseOrder));
				}
			}
		}
		return textures;
	}

	private List<RenderableTexture> resolveSlot(ModelSlot slot, Component child, ModelResolutionContext parentContext, int baseOrder) {
		ModelResolutionContext childContext = parentContext.with(child);

		// Chain of fallbacks for model resolution:
		// 1. Try to find a model for the child's ID in the slot's specific context.
		// 2. If that fails, try to find a model for the child's ID in the default context.
		// 3. If that also fails, fall back to resolving the child component's own model definition directly.

		Optional<Model> modelOpt = slot.context()
				.flatMap(ctx -> modelRegistry.find(child.id(), ctx)); // 1. Contextual lookup

		if (modelOpt.isEmpty()) {
			modelOpt = modelRegistry.find(child.id()); // 2. Default lookup
		}

		return modelOpt
				.map(model -> collectTexturesFromKnownModel(model, child, baseOrder + slot.order(), childContext))
				.orElseGet(() -> resolveComponent(child, childContext).stream() // 3. Fallback to child's own model
						.map(tex -> tex.withOrder(baseOrder + slot.order() + tex.order()))
						.toList());
	}

	private RenderableTexture getLayerTexture(ModelLayer layer, ModelResolutionContext context, int baseOrder) {
		return layer.getActiveVariant(context)
				.map(variant -> new RenderableTexture(
						variant.texture().orElse(layer.texture()),
						baseOrder + layer.order(),
						variant.offset().orElse(layer.offset().orElse(Offset.ZERO)))
				)
				.orElse(new RenderableTexture(
						layer.texture(),
						baseOrder + layer.order(),
						layer.offset().orElse(Offset.ZERO))
				);
	}

	private List<RenderableTexture> getTexturesFromTextureModel(TextureModel model, ModelResolutionContext context, int baseOrder) {
		return List.of(getLayerTexture(new ModelLayer(model.texture(), 0, model.variants(), model.offset()), context, baseOrder));
	}
}
