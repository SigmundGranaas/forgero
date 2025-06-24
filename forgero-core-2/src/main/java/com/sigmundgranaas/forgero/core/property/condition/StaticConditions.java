package com.sigmundgranaas.forgero.core.property.condition;

import com.sigmundgranaas.forgero.core.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.property.context.ResolutionContext;

/**
 * A factory class for creating common, reusable {@link StaticCondition} instances.
 * This provides a clean, human-readable API for defining structural and positional conditions
 * that are evaluated during the static "Bake" phase of resolution.
 */
public final class StaticConditions {
	private StaticConditions() {
	}

	/**
	 * Creates a condition that is true if the root component of the assembly has a specific tag.
	 *
	 * @param tag The string representation of the tag's path (e.g., "pickaxe").
	 */
	public static StaticCondition rootHasTag(String tag) {
		return (ctx) -> ctx.root().getTags().stream().anyMatch(t -> t.path().equals(tag));
	}

	/**
	 * Creates a condition that is true if the component providing the property ('self') has a specific tag.
	 *
	 * @param tag The string representation of the tag's path (e.g., "metal").
	 */
	public static StaticCondition selfHasTag(String tag) {
		return (ctx) -> ctx.self().getTags().stream().anyMatch(t -> t.path().equals(tag));
	}

	/**
	 * Creates a condition that is true only if the component being evaluated is the root of the resolution tree.
	 * Useful for properties that should only apply to a component when it's standalone, not part of a larger assembly.
	 * <p>
	 * Example: A schematic's "recipe" property should only be active when viewing the schematic item, not when it's a part of a tool.
	 */
	public static StaticCondition isRoot() {
		return ResolutionContext::isRoot;
	}

	/**
	 * Creates a condition that is true if the component is contained within a slot of a specific type.
	 * Allows for components to have different behaviors depending on where they are socketed.
	 * <p>
	 * Example: {@code selfInSlot("forgero:offensive_gem_slot")}
	 */
	public static StaticCondition selfInSlot(OpenIdentifier slotType) {
		return (ctx) -> ctx.getSlot()
				.map(slot -> slot.type().equals(slotType))
				.orElse(false);
	}

	/**
	 * Creates a condition that is true if the component has a sibling with a specific ID.
	 * "Siblings" are other components that share the same direct parent. This is ideal for creating set bonuses or synergies.
	 * <p>
	 * Example: A "Ruby of Fire" getting a bonus if it has a sibling "Sapphire of Ice" in the same hilt.
	 */
	public static StaticCondition hasSibling(OpenIdentifier siblingId) {
		return (ctx) -> ctx.getSiblings().stream()
				.anyMatch(sibling -> sibling.id().equals(siblingId));
	}

	/**
	 * Creates a condition that is true if the component is located at a specific depth in the component tree.
	 * The root is at depth 0, its direct children are at depth 1, and so on.
	 * <p>
	 * Example: A material at depth 2 (e.g., iron -> head -> pickaxe) could have different properties than a part at depth 1.
	 */
	public static StaticCondition atDepth(int depth) {
		return (ctx) -> ctx.getDepth() == depth;
	}

	/**
	 * Creates a condition that is true if a slot of a given type is filled with a component that has a specific tag.
	 * This allows for powerful cross-component conditional logic.
	 * <p>
	 * Example: A pickaxe head gets a bonus if the handle slot {@code slotContains("forgero:handle", "wood")}.
	 */
	public static StaticCondition slotContains(OpenIdentifier slotType, String tag) {
		return (ctx) -> ctx.findInRoot(slotType)
				.map(comp -> comp.getTags().stream().anyMatch(t -> t.path().equals(tag)))
				.orElse(false);
	}
}
