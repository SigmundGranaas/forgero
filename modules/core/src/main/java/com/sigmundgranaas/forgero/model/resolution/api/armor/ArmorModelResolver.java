package com.sigmundgranaas.forgero.model.resolution.api.armor;

import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.model.api.armor.ArmorModel;

import java.util.List;

/**
 * A service for resolving a Forgero component into a list of renderable 3D armor pieces.
 */
@FunctionalInterface
public interface ArmorModelResolver {
	/**
	 * Recursively resolves a component and its children into a list of armor pieces to be rendered.
	 *
	 * @param component The root component of the item to resolve.
	 * @return A list of {@link ResolvedArmorPiece} objects, each representing a distinct 3D part.
	 */
	List<ArmorModel> resolve(Component component);
}
