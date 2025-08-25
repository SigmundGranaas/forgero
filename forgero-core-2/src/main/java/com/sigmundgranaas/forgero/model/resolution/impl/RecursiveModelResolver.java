package com.sigmundgranaas.forgero.model.resolution.impl;

import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.CustomizableComponent;
import com.sigmundgranaas.forgero.core.component.api.StructuredComponent;
import com.sigmundgranaas.forgero.core.component.api.slot.UpgradeSlot;
import com.sigmundgranaas.forgero.core.component.api.structure.StructureSlot;
import com.sigmundgranaas.forgero.model.api.*;
import com.sigmundgranaas.forgero.model.api.item.CompositeModel;
import com.sigmundgranaas.forgero.model.api.item.EmptyModel;
import com.sigmundgranaas.forgero.model.api.item.Model;
import com.sigmundgranaas.forgero.model.api.item.TextureModel;
import com.sigmundgranaas.forgero.model.registry.api.item.ItemModelRegistry;
import com.sigmundgranaas.forgero.model.resolution.api.item.ItemModelResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class RecursiveModelResolver implements ItemModelResolver {
	private static final Logger LOGGER = LoggerFactory.getLogger(RecursiveModelResolver.class);

	private final ItemModelRegistry modelRegistry;

	public RecursiveModelResolver(ItemModelRegistry modelRegistry) {
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
					textures.addAll(resolveSlot(modelSlot, childComponent, context, baseOrder, composite));
				}
			}
		}

		if (component instanceof CustomizableComponent customizable) {
			// Create a map of the component's upgrade slots, keyed by their slot's id.
			Map<String, Optional<Component>> slots = customizable.getUpgradeSlots().stream()
					.collect(Collectors.toMap(slot -> slot.id().path(), UpgradeSlot::content));

			for (ModelSlot modelSlot : composite.slots().stream().toList()) {
				if(slots.containsKey(modelSlot.id())) {
					slots.get(modelSlot.id()).ifPresent(childComponent -> textures.addAll(resolveSlot(modelSlot, childComponent, context, baseOrder, composite)));
				}else{
					LOGGER.warn("No upgrade slot found in component found for slot {} in component {}",  modelSlot.id(), customizable.id());
				}
			}
		}
		return textures;
	}

	private List<RenderableTexture> resolveSlot(ModelSlot slot, Component child, ModelResolutionContext parentContext, int baseOrder, CompositeModel parentModel) {
		ModelResolutionContext childContext = parentContext.with(child);

		// 1. Find the model for the child component
		Optional<Model> modelOpt = slot.context()
				.flatMap(ctx -> modelRegistry.find(child.id(), ctx))
				.or(() -> modelRegistry.find(child.id()));

		if (modelOpt.isEmpty()) {
			// Fallback: If no model is found for the child, resolve it independently. Mount points won't apply.
			return resolveComponent(child, childContext).stream()
					.map(tex -> tex.withOrder(baseOrder + slot.order() + tex.order()))
					.toList();
		}

		Model childModel = modelOpt.get();

		// 2. Calculate the mount offset
		Offset mountOffset = calculateMountOffset(slot, parentModel, childModel);

		// 3. Collect textures from the resolved child model
		List<RenderableTexture> childTextures = collectTexturesFromKnownModel(childModel, child, baseOrder + slot.order(), childContext);

		// 4. Apply the mount offset to all collected textures
		if (mountOffset != Offset.ZERO) {
			return childTextures.stream()
					.map(texture -> new RenderableTexture(texture.texture(), texture.order(), texture.offset().add(mountOffset)))
					.collect(Collectors.toList());
		} else {
			return childTextures;
		}
	}

	private Offset calculateMountOffset(ModelSlot slot, Model parentModel, Model childModel) {
		if (slot.targetMount().isEmpty() || slot.childMount().isEmpty()) {
			return Offset.ZERO;
		}

		String targetMountName = slot.targetMount().get();
		String childMountName = slot.childMount().get();

		Offset parentOffset = parentModel.getMountPoints().stream()
				.filter(mp -> mp.name().equals(targetMountName))
				.findFirst()
				.map(MountPoint::getOffset)
				.orElse(Offset.ZERO);

		Offset childOffset = childModel.getMountPoints().stream()
				.filter(mp -> mp.name().equals(childMountName))
				.findFirst()
				.map(MountPoint::getOffset)
				.orElse(Offset.ZERO);

		return new Offset(parentOffset.x() - childOffset.x(), parentOffset.y() - childOffset.y());
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
