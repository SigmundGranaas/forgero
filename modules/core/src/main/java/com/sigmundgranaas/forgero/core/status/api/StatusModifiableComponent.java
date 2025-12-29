package com.sigmundgranaas.forgero.core.status.api;

import com.sigmundgranaas.forgero.core.component.api.Component;

import java.util.List;

/**
 * A contract for any component that can have status modifiers applied.
 * Status modifiers are enchantment-like properties (e.g., "sharp", "durable", "broken")
 * that can be added to or removed from components.
 *
 * <p>This interface parallels {@link com.sigmundgranaas.forgero.core.component.api.CustomizableComponent}
 * for upgrade slots, providing a type-safe way to manage status modifier slots.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * if (component instanceof StatusModifiableComponent modifiable) {
 *     StatusModifierSlotContainer modifiers = modifiable.statusModifiers();
 *     if (modifiers.canInstall(sharpModifier)) {
 *         Component updated = modifiable.withStatusModifiers(
 *             modifiers.tryInstall(sharpModifier).orElseThrow()
 *         );
 *     }
 * }
 * }</pre>
 */
public interface StatusModifiableComponent extends Component {

	/**
	 * Returns the status modifier slots on this component.
	 *
	 * @return The container of status modifier slots.
	 */
	StatusModifierSlotContainer statusModifiers();

	/**
	 * Creates a new instance of this component with the specified status modifiers.
	 *
	 * @param modifiers The new status modifier container.
	 * @return A new component instance with the applied status modifiers.
	 */
	Component withStatusModifiers(StatusModifierSlotContainer modifiers);

	/**
	 * Convenience method to directly access the list of StatusModifierSlot instances.
	 *
	 * @return A list of StatusModifierSlot instances.
	 */
	default List<StatusModifierSlot> getStatusModifierSlots() {
		return statusModifiers().allSlots();
	}

	/**
	 * Convenience method to check if this component has any status modifiers installed.
	 *
	 * @return true if at least one status modifier slot is filled.
	 */
	default boolean hasStatusModifiers() {
		return !statusModifiers().isEmpty();
	}

	/**
	 * Convenience method to get all installed status modifiers.
	 *
	 * @return A list of all status modifiers currently installed.
	 */
	default List<StatusModifier> getInstalledModifiers() {
		return statusModifiers().allModifiers();
	}
}
