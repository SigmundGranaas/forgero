package com.sigmundgranaas.forgero.model.resolution.api.armor;

import com.sigmundgranaas.forgero.model.api.armor.ArmorModel;
import com.sigmundgranaas.forgero.model.resolution.api.ModelResolver;

/**
 * A service for resolving a Forgero component into a list of renderable 3D armor pieces.
 *
 * Extends the generic ModelResolver interface, providing type-safe armor model resolution.
 * This demonstrates the composition-based approach where ArmorModelResolver
 * IS-A ModelResolver<ArmorModel>, allowing it to be used polymorphically.
 */
@FunctionalInterface
public interface ArmorModelResolver extends ModelResolver<ArmorModel> {
	// Inherits resolve(Component) from ModelResolver<ArmorModel>
	// Custom armor-specific resolution methods can be added here if needed
}
