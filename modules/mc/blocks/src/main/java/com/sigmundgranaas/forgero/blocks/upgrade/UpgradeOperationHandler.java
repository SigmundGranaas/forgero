package com.sigmundgranaas.forgero.blocks.upgrade;

import com.sigmundgranaas.forgero.blocks.api.StationContext;
import com.sigmundgranaas.forgero.blocks.api.StationOperationResult;
import com.sigmundgranaas.forgero.blocks.api.StationOperationResult.Failure;
import com.sigmundgranaas.forgero.blocks.api.StationOperationResult.Success;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.slot.ComponentUpgradeSlot;
import com.sigmundgranaas.forgero.core.component.api.slot.InstallationResult;

import net.minecraft.item.ItemStack;

import java.util.Optional;
import java.util.function.Function;

/**
 * Handles upgrade operations (install, remove, swap) for the Upgrade Station.
 * <p>
 * This class encapsulates the logic for modifying component upgrades,
 * using the {@link com.sigmundgranaas.forgero.core.component.api.slot.SlotManager} API
 * and {@link com.sigmundgranaas.forgero.core.component.api.slot.SlotQuery} for slot lookup.
 * <p>
 * All operations are immutable - they return new component instances.
 * <p>
 * Design principles:
 * <ul>
 *   <li>Uses SlotQuery fluent API for slot lookups instead of manual filtering</li>
 *   <li>Uses InstallationResult for rich error information</li>
 *   <li>Helper methods reduce code duplication for conversion patterns</li>
 *   <li>All ItemStack ↔ Component conversions go through StationContext</li>
 * </ul>
 */
public class UpgradeOperationHandler {

	private final StationContext context;

	/**
	 * Creates a new operation handler.
	 *
	 * @param context The station context with all services
	 */
	public UpgradeOperationHandler(StationContext context) {
		this.context = context;
	}

	// ========== PUBLIC OPERATIONS ==========

	/**
	 * Installs an upgrade into a specific slot.
	 *
	 * @param target       The target component
	 * @param slotId       The slot ID to install into
	 * @param upgradeStack The upgrade item stack
	 * @return Success with updated component, or Failure with error message
	 */
	public StationOperationResult installUpgrade(Component target, OpenIdentifier slotId, ItemStack upgradeStack) {
		return withUpgradeComponent(upgradeStack, upgrade -> {
			// Find the slot using SlotQuery API
			Optional<ComponentUpgradeSlot> slotOpt = findSlotById(target, slotId);
			if (slotOpt.isEmpty()) {
				return Failure.slotNotFound(slotId.toString());
			}

			ComponentUpgradeSlot slot = slotOpt.get();

			// Check if slot is already filled
			if (slot.isFilled()) {
				return Failure.slotAlreadyFilled();
			}

			// Validate upgrade compatibility using SlotManager API
			Optional<String> validationError = context.slotManager().validateUpgrade(slot, upgrade);
			if (validationError.isPresent()) {
				return new Failure("forgero.station.error.validation_failed", validationError.get());
			}

			// Install and wrap result
			return executeAndWrap(() -> context.slotManager().installInSlot(target, slotId, upgrade));
		});
	}

	/**
	 * Removes an upgrade from a specific slot.
	 *
	 * @param target The target component
	 * @param slotId The slot ID to remove from
	 * @return Success with updated component, or Failure with error message
	 */
	public StationOperationResult removeUpgrade(Component target, OpenIdentifier slotId) {
		// Verify slot exists and is filled
		Optional<ComponentUpgradeSlot> slotOpt = findSlotById(target, slotId);
		if (slotOpt.isEmpty()) {
			return Failure.slotNotFound(slotId.toString());
		}

		if (slotOpt.get().isEmpty()) {
			return StationOperationResult.NoOp.INSTANCE;
		}

		return executeAndWrap(() -> context.slotManager().removeFromSlot(target, slotId));
	}

	/**
	 * Swaps an upgrade in a slot with a new one.
	 * <p>
	 * The old upgrade should be returned to inventory by the caller.
	 *
	 * @param target     The target component
	 * @param slotId     The slot ID to swap in
	 * @param newUpgrade The new upgrade item stack
	 * @return Success with updated component, or Failure with error message
	 */
	public StationOperationResult swapUpgrade(Component target, OpenIdentifier slotId, ItemStack newUpgrade) {
		return withUpgradeComponent(newUpgrade, upgrade -> {
			// Find and validate slot
			Optional<ComponentUpgradeSlot> slotOpt = findSlotById(target, slotId);
			if (slotOpt.isEmpty()) {
				return Failure.slotNotFound(slotId.toString());
			}

			// Validate new upgrade is compatible
			Optional<String> validationError = context.slotManager().validateUpgrade(slotOpt.get(), upgrade);
			if (validationError.isPresent()) {
				return new Failure("forgero.station.error.validation_failed", validationError.get());
			}

			// Use installOrReplace for atomic swap
			return executeAndWrap(() -> context.slotManager().installOrReplace(target, slotId, upgrade));
		});
	}

	/**
	 * Auto-installs an upgrade by finding a compatible empty slot.
	 * <p>
	 * Uses the SlotManager's {@link InstallationResult} for rich feedback.
	 *
	 * @param target       The target component
	 * @param upgradeStack The upgrade item stack
	 * @return Success with updated component and slot used, or Failure
	 */
	public StationOperationResult autoInstall(Component target, ItemStack upgradeStack) {
		return withUpgradeComponent(upgradeStack, upgrade -> {
			// Use SlotManager's high-level install with InstallationResult
			InstallationResult result = context.slotManager().install(target, upgrade);

			if (!result.success()) {
				// Map InstallationResult error to our Failure type
				String error = result.errorMessage().orElse("No compatible slot found");
				return new Failure("forgero.station.error.install_failed", error);
			}

			// Convert the updated component back to ItemStack
			Component updated = result.component().orElseThrow(
					() -> new IllegalStateException("Successful installation must have component"));

			return wrapSuccess(updated);
		});
	}

	/**
	 * Removes all upgrades from a component.
	 *
	 * @param target The target component
	 * @return Success with stripped component
	 */
	public StationOperationResult removeAllUpgrades(Component target) {
		// Check if there are any upgrades to remove
		if (context.slotManager().countFilledSlots(target) == 0) {
			return StationOperationResult.NoOp.INSTANCE;
		}

		return executeAndWrap(() -> context.slotManager().removeAllUpgrades(target));
	}

	/**
	 * Gets the old upgrade from a slot before it's replaced.
	 * <p>
	 * This is useful for returning the old upgrade to player inventory.
	 *
	 * @param target The component
	 * @param slotId The slot ID
	 * @return The upgrade ItemStack if slot was filled, empty otherwise
	 */
	public Optional<ItemStack> getOldUpgrade(Component target, OpenIdentifier slotId) {
		return findSlotById(target, slotId)
				.filter(ComponentUpgradeSlot::isFilled)
				.flatMap(ComponentUpgradeSlot::getContent)
				.flatMap(context.converter()::toStack);
	}

	/**
	 * Checks if an upgrade can be installed in any slot.
	 *
	 * @param target       The target component
	 * @param upgradeStack The upgrade to check
	 * @return true if installation is possible
	 */
	public boolean canInstall(Component target, ItemStack upgradeStack) {
		return context.converter().toComponent(upgradeStack)
				.map(upgrade -> context.slotManager().canInstall(target, upgrade))
				.orElse(false);
	}

	/**
	 * Checks if an upgrade can be installed in a specific slot.
	 *
	 * @param target       The target component
	 * @param slotId       The slot to check
	 * @param upgradeStack The upgrade to check
	 * @return true if installation is possible
	 */
	public boolean canInstallInSlot(Component target, OpenIdentifier slotId, ItemStack upgradeStack) {
		return context.converter().toComponent(upgradeStack)
				.flatMap(upgrade -> findSlotById(target, slotId)
						.filter(ComponentUpgradeSlot::isEmpty)
						.map(slot -> context.slotManager().isCompatible(slot, upgrade)))
				.orElse(false);
	}

	// ========== PRIVATE HELPERS ==========

	/**
	 * Finds a slot by ID using the SlotQuery fluent API.
	 * <p>
	 * This is the proper way to look up slots instead of manual stream filtering.
	 */
	private Optional<ComponentUpgradeSlot> findSlotById(Component target, OpenIdentifier slotId) {
		return context.slotManager().queryUpgradeSlots(target)
				.matching(slot -> slot.id().equals(slotId))
				.first();
	}

	/**
	 * Helper that converts an ItemStack to Component and applies an operation.
	 * <p>
	 * This reduces the repeated pattern of:
	 * <pre>{@code
	 * Optional<Component> opt = converter.toComponent(stack);
	 * if (opt.isEmpty()) return Failure.notForgeroItem();
	 * Component upgrade = opt.get();
	 * // ... use upgrade
	 * }</pre>
	 *
	 * @param stack     The ItemStack to convert
	 * @param operation The operation to apply if conversion succeeds
	 * @return The operation result, or Failure.notForgeroItem() if conversion fails
	 */
	private StationOperationResult withUpgradeComponent(
			ItemStack stack,
			Function<Component, StationOperationResult> operation) {
		return context.converter().toComponent(stack)
				.map(operation)
				.orElseGet(Failure::notForgeroItem);
	}

	/**
	 * Helper that wraps a successful component in a StationOperationResult.
	 * <p>
	 * This handles the Component → ItemStack conversion and wrapping in Success.
	 *
	 * @param updated The updated component
	 * @return Success with the component and its ItemStack representation
	 */
	private StationOperationResult wrapSuccess(Component updated) {
		return context.converter().toStack(updated)
				.map(stack -> (StationOperationResult) new Success(updated, stack))
				.orElseGet(Failure::conversionFailed);
	}

	/**
	 * Helper that executes a component-returning operation and wraps the result.
	 * <p>
	 * This handles exceptions and conversion in one place.
	 *
	 * @param operation The operation that returns a Component
	 * @return Success with the result, or Failure if operation throws or conversion fails
	 */
	private StationOperationResult executeAndWrap(ComponentOperation operation) {
		try {
			Component updated = operation.execute();
			return wrapSuccess(updated);
		} catch (IllegalArgumentException e) {
			return new Failure("forgero.station.error.operation_failed", e.getMessage());
		}
	}

	/**
	 * Functional interface for component-returning operations that may throw.
	 */
	@FunctionalInterface
	private interface ComponentOperation {
		Component execute() throws IllegalArgumentException;
	}

	// ========== FACTORY ==========

	/**
	 * Factory method.
	 */
	public static UpgradeOperationHandler create(StationContext context) {
		return new UpgradeOperationHandler(context);
	}
}
