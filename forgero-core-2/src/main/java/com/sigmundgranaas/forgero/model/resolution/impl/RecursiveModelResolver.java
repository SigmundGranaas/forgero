package com.sigmundgranaas.forgero.model.resolution.impl;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.*;
import com.sigmundgranaas.forgero.model.api.CompositeModel;
import com.sigmundgranaas.forgero.model.resolution.api.LayeredTexture;
import com.sigmundgranaas.forgero.model.api.Model;
import com.sigmundgranaas.forgero.model.resolution.api.ModelResolver;
import com.sigmundgranaas.forgero.model.registry.api.ModelRegistry;
import com.sigmundgranaas.forgero.model.api.StaticModel;
import com.sigmundgranaas.forgero.model.resolution.api.TextureLayer;

import java.util.*;
public class RecursiveModelResolver implements ModelResolver {
	private final ModelRegistry modelRegistry;
	public RecursiveModelResolver(ModelRegistry modelRegistry) { this.modelRegistry = modelRegistry; }
	@Override
	public Optional<LayeredTexture> resolve(Component component) {
		var context = new ResolutionContext();
		var layers = resolveComponent(component, 0, context, null);
		if (layers.isEmpty()) { return Optional.empty(); }
		layers.sort(TextureLayer::compareTo);
		return Optional.of(new LayeredTexture(layers));
	}
	private List<TextureLayer> resolveComponent(Component component, int baseOrder, ResolutionContext context, Slot parentSlot) {
		OpenIdentifier modelId = context.getModelId(parentSlot, component);
		return modelRegistry.find(modelId)
				.map(model -> processModel(model, component, baseOrder, context))
				.orElseGet(Collections::emptyList);
	}
	private List<TextureLayer> processModel(Model model, Component component, int baseOrder, ResolutionContext context) {
		List<TextureLayer> layers = new ArrayList<>();
		if (model instanceof CompositeModel composite) {
			context.addModelOverrides(composite.modelSelectors());
			composite.self().forEach(selfLayer -> layers.add(new TextureLayer(selfLayer.texture(), baseOrder + selfLayer.order())));
			handleParts(component, composite, baseOrder, context, layers);
			handleUpgrades(component, composite, baseOrder, context, layers);
		} else if (model instanceof StaticModel staticModel) {
			layers.add(new TextureLayer(staticModel.texture(), baseOrder));
		}
		return layers;
	}
	private void handleParts(Component component, CompositeModel model, int baseOrder, ResolutionContext context, List<TextureLayer> layers) {
		if (component instanceof StructuredComponent structured) {
			for (CompositeModel.Part partInfo : model.parts()) {
				findSlot(structured.structure().slots().values(), partInfo.slot())
						.ifPresent(slot -> slot.get().ifPresent(partComponent -> layers.addAll(resolveComponent(partComponent, baseOrder + partInfo.order(), context, slot))));
			}
		}
	}
	private void handleUpgrades(Component component, CompositeModel model, int baseOrder, ResolutionContext context, List<TextureLayer> layers) {
		if (component instanceof CustomizableComponent customizable) {
			for (CompositeModel.Part upgradeInfo : model.upgrades()) {
				findSlot(customizable.upgrades().slots(), upgradeInfo.slot())
						.ifPresent(slot -> slot.get().ifPresent(upgradeComponent -> layers.addAll(resolveComponent(upgradeComponent, baseOrder + upgradeInfo.order(), context, slot))));
			}
		}
	}
	private Optional<? extends Slot> findSlot(Collection<? extends Slot> slots, String id) { return slots.stream().filter(s -> s.id().path().equals(id)).findFirst(); }
	private static class ResolutionContext {
		private final Map<String, OpenIdentifier> modelOverrides = new HashMap<>();
		public void addModelOverrides(List<CompositeModel.ModelSelector> selectors) { selectors.forEach(s -> modelOverrides.put(s.targetSlot(), new OpenIdentifier(s.model()))); }
		public OpenIdentifier getModelId(Slot slot, Component component) {
			if (slot != null && modelOverrides.containsKey(slot.id().path())) { return modelOverrides.get(slot.id().path()); }
			return component.id();
		}
	}
}
