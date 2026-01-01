package com.sigmundgranaas.forgero.model.api;

import java.util.List;

/**
 * Capability interface for models that have slots where other components can be attached.
 * This enables compositional model building where child components can render within parent models.
 */
@FunctionalInterface
public interface Slotted {
	/**
	 * @return The slots where other components can be attached to this model.
	 */
	List<ModelSlot> slots();
}
