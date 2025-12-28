package com.sigmundgranaas.forgero.core.component.api.slot.impl;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.CustomizableComponent;
import com.sigmundgranaas.forgero.core.component.api.StructuredComponent;
import com.sigmundgranaas.forgero.core.component.api.slot.*;
import com.sigmundgranaas.forgero.core.component.api.structure.ComponentPart;
import com.sigmundgranaas.forgero.core.component.mutation.api.ComponentMutater;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of {@link SlotManager} that delegates low-level operations
 * to {@link ComponentMutater}.
 * <p>
 * This class provides high-level slot management operations while maintaining
 * immutability - all operations return new component instances.
 */
public class SlotManagerImpl implements SlotManager {

	private final ComponentMutater mutater;

	/**
	 * Creates a new SlotManager implementation.
	 *
	 * @param mutater The component mutater for low-level slot operations
	 */
	public SlotManagerImpl(ComponentMutater mutater) {
		this.mutater = mutater;
	}

	// ========== QUERY OPERATIONS ==========

	@Override
	public SlotQuery<ComponentUpgradeSlot> queryUpgradeSlots(Component component) {
		return new SlotQueryImpl<>(getAllUpgradeSlots(component));
	}

	@Override
	public List<ComponentPart> getStructureParts(Component component) {
		if (component instanceof StructuredComponent structured) {
			return structured.structure().allParts().stream().toList();
		}
		return Collections.emptyList();
	}

	@Override
	public List<ComponentUpgradeSlot> getAllUpgradeSlots(Component component) {
		if (component instanceof CustomizableComponent customizable) {
			return customizable.upgrades().slots().all().stream()
					.filter(slot -> slot instanceof ComponentUpgradeSlot)
					.map(slot -> (ComponentUpgradeSlot) slot)
					.toList();
		}
		return Collections.emptyList();
	}

	@Override
	public List<ComponentUpgradeSlot> getEmptyUpgradeSlots(Component component) {
		return getAllUpgradeSlots(component).stream()
				.filter(ComponentUpgradeSlot::isEmpty)
				.toList();
	}

	@Override
	public List<ComponentUpgradeSlot> getFilledUpgradeSlots(Component component) {
		return getAllUpgradeSlots(component).stream()
				.filter(ComponentUpgradeSlot::isFilled)
				.toList();
	}

	@Override
	public List<Component> getInstalledUpgrades(Component component) {
		if (component instanceof CustomizableComponent customizable) {
			return customizable.upgrades().filledContents();
		}
		return Collections.emptyList();
	}

	// ========== COMPATIBILITY CHECKING ==========

	@Override
	public boolean canInstall(Component target, Component upgrade) {
		return findCompatibleSlot(target, upgrade).isPresent();
	}

	@Override
	public Optional<ComponentUpgradeSlot> findCompatibleSlot(Component target, Component upgrade) {
		return getEmptyUpgradeSlots(target).stream()
				.filter(slot -> slot.validator().test(upgrade))
				.findFirst();
	}

	@Override
	public List<ComponentUpgradeSlot> findAllCompatibleSlots(Component target, Component upgrade) {
		return getEmptyUpgradeSlots(target).stream()
				.filter(slot -> slot.validator().test(upgrade))
				.toList();
	}

	@Override
	public boolean isCompatible(ComponentUpgradeSlot slot, Component upgrade) {
		return slot.validator().test(upgrade);
	}

	@Override
	public Optional<String> validateUpgrade(ComponentUpgradeSlot slot, Component upgrade) {
		return slot.validator().validate(upgrade, slot.id());
	}

	// ========== INSTALLATION OPERATIONS ==========

	@Override
	public InstallationResult install(Component target, Component upgrade) {
		if (target == null) {
			throw new IllegalArgumentException("Target component cannot be null");
		}
		if (upgrade == null) {
			throw new IllegalArgumentException("Upgrade component cannot be null");
		}

		Optional<ComponentUpgradeSlot> compatibleSlot = findCompatibleSlot(target, upgrade);

		if (compatibleSlot.isEmpty()) {
			return InstallationResult.failure(
					"No compatible slot found for upgrade: " + upgrade.id() +
							" (type: " + upgrade.getTypeIdentifier() + ")");
		}

		try {
			Component result = mutater.setSlot(target, compatibleSlot.get().id(), upgrade);
			return InstallationResult.success(result, compatibleSlot.get().id());
		} catch (IllegalArgumentException e) {
			return InstallationResult.failure("Installation failed: " + e.getMessage());
		}
	}

	@Override
	public Component installInSlot(Component target, OpenIdentifier slotId, Component upgrade) {
		// Validate slot exists and is empty
		ComponentUpgradeSlot slot = getComponentUpgradeSlot(target, slotId)
				.orElseThrow(() -> new IllegalArgumentException("Slot not found: " + slotId));

		if (slot.isFilled()) {
			throw new IllegalArgumentException("Slot already filled: " + slotId +
					" (contains: " + slot.content().map(Component::id).orElse(null) + ")");
		}

		// Validate upgrade
		slot.validator().validate(upgrade, slotId).ifPresent(error -> {
			throw new IllegalArgumentException(error);
		});

		return mutater.setSlot(target, slotId, upgrade);
	}

	@Override
	public Component installOrReplace(Component target, OpenIdentifier slotId, Component upgrade) {
		// Just validate slot exists and upgrade is compatible
		ComponentUpgradeSlot slot = getComponentUpgradeSlot(target, slotId)
				.orElseThrow(() -> new IllegalArgumentException("Slot not found: " + slotId));

		// Validate upgrade
		slot.validator().validate(upgrade, slotId).ifPresent(error -> {
			throw new IllegalArgumentException(error);
		});

		return mutater.setSlot(target, slotId, upgrade);
	}

	// ========== REMOVAL OPERATIONS ==========

	@Override
	public Component removeUpgrade(Component target, OpenIdentifier upgradeId) {
		Optional<ComponentUpgradeSlot> slotToRemove = getFilledUpgradeSlots(target).stream()
				.filter(slot -> slot.content().map(c -> c.id().equals(upgradeId)).orElse(false))
				.findFirst();

		if (slotToRemove.isEmpty()) {
			return target; // Not found, return unchanged
		}

		return mutater.removeSlot(target, slotToRemove.get().id());
	}

	@Override
	public Component removeFromSlot(Component target, OpenIdentifier slotId) {
		return mutater.removeSlot(target, slotId);
	}

	@Override
	public Component removeAllUpgrades(Component target) {
		if (!(target instanceof CustomizableComponent customizable)) {
			return target;
		}

		// Empty all ComponentUpgradeSlot instances
		ComponentUpgrades emptyUpgrades = ComponentUpgrades.of(
				customizable.upgrades().slots().all().stream()
						.filter(slot -> slot instanceof ComponentUpgradeSlot)
						.map(slot -> ((ComponentUpgradeSlot) slot).empty())
						.toList()
		);

		return customizable.withUpgrades(emptyUpgrades);
	}

	// ========== UTILITY OPERATIONS ==========

	@Override
	public int countComponentUpgradeSlots(Component component) {
		return getAllUpgradeSlots(component).size();
	}

	@Override
	public int countFilledSlots(Component component) {
		return getFilledUpgradeSlots(component).size();
	}

	@Override
	public int countEmptySlots(Component component) {
		return getEmptyUpgradeSlots(component).size();
	}

	@Override
	public boolean areAllSlotsFilled(Component component) {
		if (component instanceof CustomizableComponent customizable) {
			return customizable.upgrades().allFilled();
		}
		return false;
	}

	@Override
	public boolean hasComponentUpgradeSlots(Component component) {
		return component instanceof CustomizableComponent customizable
				&& !customizable.upgrades().isEmpty();
	}

	// ========== PRIVATE HELPER METHODS ==========

	/**
	 * Gets an upgrade slot by ID.
	 *
	 * @param component The component to search
	 * @param slotId    The slot ID
	 * @return The upgrade slot if found
	 */
	private Optional<ComponentUpgradeSlot> getComponentUpgradeSlot(Component component, OpenIdentifier slotId) {
		if (component instanceof CustomizableComponent customizable) {
			return customizable.upgrades().get(slotId);
		}
		return Optional.empty();
	}
}
