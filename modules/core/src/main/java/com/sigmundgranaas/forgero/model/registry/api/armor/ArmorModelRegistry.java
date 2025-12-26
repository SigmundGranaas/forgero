package com.sigmundgranaas.forgero.model.registry.api.armor;

import com.sigmundgranaas.forgero.model.api.armor.ArmorModel;
import com.sigmundgranaas.forgero.model.registry.api.ModelRegistry;

/**
 * Registry for armor models.
 * Extends the generic ModelRegistry interface, providing type-safe armor model operations.
 *
 * This demonstrates the composition-based approach where ArmorModelRegistry
 * IS-A ModelRegistry<ArmorModel>, allowing it to be used polymorphically.
 */
public interface ArmorModelRegistry extends ModelRegistry<ArmorModel> {
	// Inherits all methods from ModelRegistry<ArmorModel>
	// Custom armor-specific methods can be added here if needed
}
