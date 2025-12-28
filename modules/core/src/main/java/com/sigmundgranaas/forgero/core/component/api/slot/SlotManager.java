package com.sigmundgranaas.forgero.core.component.api.slot;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.structure.ComponentPart;

import java.util.List;
import java.util.Optional;

/**
 * High-level service for querying and manipulating component slots.
 * <p>
 * This service provides convenient operations for:
 * <ul>
 *   <li>Querying available upgrade slots</li>
 *   <li>Checking upgrade compatibility</li>
 *   <li>Installing upgrades with automatic slot finding</li>
 *   <li>Removing upgrades by component or slot ID</li>
 * </ul>
 * <p>
 * All operations return new component instances (immutability preserved).
 * <p>
 * Usage via {@code ForgeroServices}:
 * <pre>{@code
 * SlotManager manager = services.slotManager();
 *
 * // Check if upgrade can be installed
 * if (manager.canInstall(tool, gemUpgrade)) {
 *     // Auto-find compatible slot and install
 *     InstallationResult result = manager.install(tool, gemUpgrade);
 *     if (result.success()) {
 *         Component upgraded = result.orElseThrow();
 *     }
 * }
 *
 * // Query empty gem slots
 * List<ComponentUpgradeSlot> gemSlots = manager.queryComponentUpgradeSlots(tool)
 *     .ofType(OpenIdentifier.of("forgero:gem"))
 *     .onlyEmpty()
 *     .execute();
 * }</pre>
 *
 * @see com.sigmundgranaas.forgero.core.component.mutation.api.ComponentMutater ComponentMutater for low-level slot operations
 * @see SlotQuery for advanced slot filtering
 */
public interface SlotManager {

	//
	// === QUERY OPERATIONS ===
	//

	/**
	 * Creates a fluent query builder for filtering upgrade slots.
	 * <p>
	 * Example usage:
	 * <pre>{@code
	 * List<ComponentUpgradeSlot> slots = manager.queryComponentUpgradeSlots(component)
	 *     .ofType(BINDING_TYPE)
	 *     .onlyEmpty()
	 *     .execute();
	 * }</pre>
	 *
	 * @param component The component to query
	 * @return A slot query builder
	 */
	SlotQuery<ComponentUpgradeSlot> queryComponentUpgradeSlots(Component component);

	/**
	 * Gets all structure parts from a component.
	 * Note: Structure parts are immutable and not queryable like slots.
	 *
	 * @param component The component to inspect
	 * @return List of all structure parts
	 */
	List<ComponentPart> getStructureParts(Component component);

	/**
	 * Gets all upgrade slots (both filled and empty).
	 *
	 * @param component The component to inspect
	 * @return List of all upgrade slots, empty if component has no upgrades
	 */
	List<ComponentUpgradeSlot> getAllComponentUpgradeSlots(Component component);

	/**
	 * Gets only empty upgrade slots.
	 *
	 * @param component The component to inspect
	 * @return List of empty upgrade slots
	 */
	List<ComponentUpgradeSlot> getEmptyComponentUpgradeSlots(Component component);

	/**
	 * Gets only filled upgrade slots.
	 *
	 * @param component The component to inspect
	 * @return List of filled upgrade slots
	 */
	List<ComponentUpgradeSlot> getFilledComponentUpgradeSlots(Component component);

	/**
	 * Gets all upgrade components currently installed.
	 *
	 * @param component The component to inspect
	 * @return List of components filling upgrade slots
	 */
	List<Component> getInstalledUpgrades(Component component);

	//
	// === COMPATIBILITY CHECKING ===
	//

	/**
	 * Checks if an upgrade can be installed in any available slot.
	 * <p>
	 * This checks:
	 * <ul>
	 *   <li>Component is customizable (has upgrade slots)</li>
	 *   <li>At least one empty slot exists</li>
	 *   <li>The upgrade passes validation for at least one empty slot</li>
	 * </ul>
	 *
	 * @param target  The component to upgrade
	 * @param upgrade The upgrade to install
	 * @return true if installation is possible
	 */
	boolean canInstall(Component target, Component upgrade);

	/**
	 * Finds the first compatible empty slot for an upgrade.
	 * <p>
	 * This iterates through empty upgrade slots in order and returns the first
	 * slot where the upgrade passes validation.
	 *
	 * @param target  The component to upgrade
	 * @param upgrade The upgrade to install
	 * @return The first compatible slot, or empty if none found
	 */
	Optional<ComponentUpgradeSlot> findCompatibleSlot(Component target, Component upgrade);

	/**
	 * Finds all compatible empty slots for an upgrade.
	 *
	 * @param target  The component to upgrade
	 * @param upgrade The upgrade to install
	 * @return List of all compatible empty slots
	 */
	List<ComponentUpgradeSlot> findAllCompatibleSlots(Component target, Component upgrade);

	/**
	 * Checks if a specific slot can accept the upgrade.
	 *
	 * @param slot    The slot to check
	 * @param upgrade The upgrade to validate
	 * @return true if the slot's validator accepts the upgrade
	 */
	boolean isCompatible(ComponentUpgradeSlot slot, Component upgrade);

	/**
	 * Validates an upgrade against a slot and returns error message if invalid.
	 *
	 * @param slot    The slot to validate against
	 * @param upgrade The upgrade to validate
	 * @return Error message if invalid, empty if valid
	 */
	Optional<String> validateUpgrade(ComponentUpgradeSlot slot, Component upgrade);

	//
	// === INSTALLATION OPERATIONS ===
	//

	/**
	 * Installs an upgrade by automatically finding a compatible empty slot.
	 * <p>
	 * This is the highest-level installation method, equivalent to the legacy
	 * {@code SlotContainer.set(State)} operation.
	 * <p>
	 * Process:
	 * <ol>
	 *   <li>Find first compatible empty slot using {@link #findCompatibleSlot}</li>
	 *   <li>Install upgrade in that slot</li>
	 *   <li>Return new component instance</li>
	 * </ol>
	 * <p>
	 * Example:
	 * <pre>{@code
	 * InstallationResult result = manager.install(tool, gemUpgrade);
	 * if (result.success()) {
	 *     Component upgraded = result.component().orElseThrow();
	 *     OpenIdentifier slotUsed = result.slotId().orElseThrow();
	 * } else {
	 *     String error = result.errorMessage().orElse("Unknown error");
	 * }
	 * }</pre>
	 *
	 * @param target  The component to upgrade
	 * @param upgrade The upgrade to install
	 * @return Installation result with component if successful, or error if failed
	 * @throws IllegalArgumentException if target is null or upgrade is null
	 */
	InstallationResult install(Component target, Component upgrade);

	/**
	 * Installs an upgrade in a specific slot by ID.
	 * <p>
	 * This validates the upgrade against the slot before installation.
	 * The slot must be empty.
	 *
	 * @param target  The component to upgrade
	 * @param slotId  The ID of the slot to fill
	 * @param upgrade The upgrade to install
	 * @return New component with upgrade installed
	 * @throws IllegalArgumentException if slot not found, slot is filled,
	 *                                  or upgrade fails validation
	 */
	Component installInSlot(Component target, OpenIdentifier slotId, Component upgrade);

	/**
	 * Installs an upgrade, replacing existing content if the slot is filled.
	 * <p>
	 * This is useful for swapping upgrades.
	 *
	 * @param target  The component to upgrade
	 * @param slotId  The ID of the slot
	 * @param upgrade The upgrade to install
	 * @return New component with upgrade installed
	 * @throws IllegalArgumentException if slot not found or upgrade fails validation
	 */
	Component installOrReplace(Component target, OpenIdentifier slotId, Component upgrade);

	//
	// === REMOVAL OPERATIONS ===
	//

	/**
	 * Removes an upgrade by its component ID.
	 * <p>
	 * This searches all filled upgrade slots for a component matching the ID
	 * and empties that slot.
	 *
	 * @param target    The component to modify
	 * @param upgradeId The ID of the upgrade to remove
	 * @return New component with upgrade removed, or original if not found
	 */
	Component removeUpgrade(Component target, OpenIdentifier upgradeId);

	/**
	 * Removes an upgrade from a specific slot by ID.
	 *
	 * @param target The component to modify
	 * @param slotId The ID of the slot to empty
	 * @return New component with slot emptied
	 * @throws IllegalArgumentException if slot not found or slot is required
	 */
	Component removeFromSlot(Component target, OpenIdentifier slotId);

	/**
	 * Removes all upgrades, emptying all upgrade slots.
	 * <p>
	 * Equivalent to legacy {@code SlotContainer.strip()}.
	 *
	 * @param target The component to modify
	 * @return New component with all upgrade slots emptied
	 */
	Component removeAllUpgrades(Component target);

	//
	// === UTILITY OPERATIONS ===
	//

	/**
	 * Counts the total number of upgrade slots.
	 *
	 * @param component The component to inspect
	 * @return Number of upgrade slots (filled + empty)
	 */
	int countComponentUpgradeSlots(Component component);

	/**
	 * Counts filled upgrade slots.
	 *
	 * @param component The component to inspect
	 * @return Number of filled slots
	 */
	int countFilledSlots(Component component);

	/**
	 * Counts empty upgrade slots.
	 *
	 * @param component The component to inspect
	 * @return Number of empty slots
	 */
	int countEmptySlots(Component component);

	/**
	 * Checks if all upgrade slots are filled.
	 *
	 * @param component The component to inspect
	 * @return true if component has upgrade slots and all are filled
	 */
	boolean areAllSlotsFilled(Component component);

	/**
	 * Checks if the component has any upgrade slots.
	 *
	 * @param component The component to inspect
	 * @return true if component is customizable and has at least one upgrade slot
	 */
	boolean hasComponentUpgradeSlots(Component component);
}
