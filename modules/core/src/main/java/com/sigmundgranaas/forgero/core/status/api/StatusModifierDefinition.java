package com.sigmundgranaas.forgero.core.status.api;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;

import java.util.Set;

/**
 * Defines the target applicability and chance for a status modifier.
 * Loaded from JSON data packs (data/forgero/status_modifiers/).
 *
 * This separates the modifier's properties from its targeting rules,
 * allowing the same modifier definition to be referenced by ID.
 */
public interface StatusModifierDefinition {

	/**
	 * @return The modifier this definition describes
	 */
	StatusModifier modifier();

	/**
	 * @return Component types this modifier can apply to (e.g., "forgero:tool", "forgero:weapon")
	 */
	Set<OpenIdentifier> targetTypes();

	/**
	 * @return Specific component IDs this modifier can apply to (empty = all matching types)
	 */
	Set<OpenIdentifier> targetIds();

	/**
	 * @return Chance of naturally occurring (0.0-1.0, for loot generation, etc.)
	 */
	float chance();

	/**
	 * Checks if this modifier can be applied to a component with the given tags.
	 *
	 * @param componentTags The tags of the target component
	 * @return true if any target type matches a component tag
	 */
	default boolean isApplicableTo(Set<OpenIdentifier> componentTags) {
		if (targetTypes().isEmpty()) {
			return true; // No type restriction
		}
		for (OpenIdentifier type : targetTypes()) {
			if (componentTags.contains(type)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks if this modifier can be applied to a specific component ID.
	 *
	 * @param componentId The ID of the target component
	 * @return true if no ID restriction or ID is in the target set
	 */
	default boolean isApplicableToId(OpenIdentifier componentId) {
		if (targetIds().isEmpty()) {
			return true; // No ID restriction
		}
		return targetIds().contains(componentId);
	}
}
