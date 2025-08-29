package com.sigmundgranaas.forgero.model.resolution.impl;

import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.StructuredComponent;
import com.sigmundgranaas.forgero.model.api.armor.ArmorModel;
import com.sigmundgranaas.forgero.model.registry.api.armor.ArmorModelRegistry;
import com.sigmundgranaas.forgero.model.resolution.api.armor.ArmorModelResolver;

import java.util.ArrayList;
import java.util.List;

public class RecursiveArmorModelResolver implements ArmorModelResolver {

	private final ArmorModelRegistry armorModelRegistry;

	public RecursiveArmorModelResolver(ArmorModelRegistry armorModelRegistry) {
		this.armorModelRegistry = armorModelRegistry;
	}

	@Override
	public List<ArmorModel> resolve(Component component) {
		List<ArmorModel> models = new ArrayList<>();
		resolveRecursively(component, models);
		return models;
	}

	private void resolveRecursively(Component component, List<ArmorModel> models) {
		// 1. Check if the current component has its own ArmorModel definition.
		armorModelRegistry.find(component.id()).ifPresent(models::add);

		// 2. Recurse into children in slots.
		// This allows an upgrade to add a new 3D part to a base item.
		if (component instanceof StructuredComponent structured) {
			structured.structure().slots().forEach((id, slot) -> {
				if (slot.content() != null && !slot.content().id().path().contains("empty")) {
					resolveRecursively(slot.content(), models);
				}
			});
		}
	}
}
