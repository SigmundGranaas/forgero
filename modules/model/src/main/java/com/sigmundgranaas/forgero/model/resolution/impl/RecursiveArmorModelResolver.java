package com.sigmundgranaas.forgero.model.resolution.impl;

import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.model.api.armor.ArmorModel;
import com.sigmundgranaas.forgero.model.registry.api.armor.ArmorModelRegistry;
import com.sigmundgranaas.forgero.model.resolution.api.armor.ArmorModelResolver;

import java.util.List;

/**
 * Armor model resolver using pure composition.
 *
 * This class COMPOSES a RecursiveModelResolutionStrategy rather than
 * implementing the recursive logic itself or extending an abstract base class.
 * This demonstrates the composition pattern where behavior is delegated
 * to contained objects.
 */
public class RecursiveArmorModelResolver implements ArmorModelResolver {
	// Composition: CONTAINS a strategy, delegates resolution to it
	private final RecursiveModelResolutionStrategy<ArmorModel> strategy;

	public RecursiveArmorModelResolver(ArmorModelRegistry armorModelRegistry) {
		// Compose the strategy with custom behavior
		this.strategy = new RecursiveModelResolutionStrategy<>(armorModelRegistry);
	}

	@Override
	public List<ArmorModel> resolve(Component component) {
		// Delegate all work to the composed strategy
		return strategy.resolve(component);
	}
}

