package com.sigmundgranaas.forgero.model.api;


import java.util.Optional;

/**
 * Represents a slot within a CompositeModel where another component can be attached.
 *
 * @param id          The identifier for the slot, which should match a component part's ID.
 * @param order       The base rendering order for the component placed in this slot.
 * @param context     An optional context string provided to the component in this slot.
 * @param targetMount The name of the mount point on the parent model to align to.
 * @param childMount  The name of the mount point on the child model that will be aligned.
 * @param dynamicKey  An optional key to look up a component from the dynamic state map.
 *                    When set, the component is retrieved from dynamicState.get(dynamicKey)
 *                    instead of from the parent component's structure/upgrades.
 *                    Used for rendering dynamic content like equipped arrows.
 */
public record ModelSlot(
		String id,
		int order,
		Optional<String> context,
		Optional<String> targetMount,
		Optional<String> childMount,
		Optional<String> dynamicKey
) {
	/**
	 * Constructor for backwards compatibility - creates a slot without dynamicKey.
	 */
	public ModelSlot(String id, int order, Optional<String> context, Optional<String> targetMount, Optional<String> childMount) {
		this(id, order, context, targetMount, childMount, Optional.empty());
	}

	/**
	 * Returns true if this slot should be resolved from dynamic state rather than component structure.
	 */
	public boolean isDynamic() {
		return dynamicKey.isPresent();
	}
}
