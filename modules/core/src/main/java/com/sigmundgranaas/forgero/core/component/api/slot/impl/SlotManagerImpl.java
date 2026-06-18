package com.sigmundgranaas.forgero.core.component.api.slot.impl;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.CustomizableComponent;
import com.sigmundgranaas.forgero.core.component.api.Slot;
import com.sigmundgranaas.forgero.core.component.api.StructuredComponent;
import com.sigmundgranaas.forgero.core.component.api.slot.*;
import com.sigmundgranaas.forgero.core.component.api.structure.ComponentPart;
import com.sigmundgranaas.forgero.core.component.mutation.api.ComponentMutater;

import java.util.ArrayList;
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
		List<ComponentUpgradeSlot> slots = new ArrayList<>();
		collectAllUpgradeSlotsRecursive(component, slots);
		return slots;
	}

	@Override
	public List<Slot> getAllSlots(Component component) {
		return mutater.getAllSlots(component);
	}

	@Override
	public List<Slot> getEmptySlots(Component component) {
		return getAllSlots(component).stream()
				.filter(Slot::acceptsComponent)
				.filter(slot -> slot.componentContent().isEmpty())
				.toList();
	}

	@Override
	public List<Slot> getFilledSlots(Component component) {
		return getAllSlots(component).stream()
				.filter(slot -> slot.componentContent().isPresent())
				.toList();
	}

	/**
	 * Recursively collects all upgrade slots from a component and its nested structure parts.
	 */
	private void collectAllUpgradeSlotsRecursive(Component component, List<ComponentUpgradeSlot> slots) {
		// Collect direct slots
		if (component instanceof CustomizableComponent customizable) {
			customizable.upgrades().slots().all().stream()
					.filter(slot -> slot instanceof ComponentUpgradeSlot)
					.map(slot -> (ComponentUpgradeSlot) slot)
					.forEach(slots::add);
		}

		// Recurse into structure parts
		if (component instanceof StructuredComponent structured) {
			for (ComponentPart part : structured.structure().allParts()) {
				collectAllUpgradeSlotsRecursive(part.content(), slots);
			}
		}
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
		List<Component> upgrades = new ArrayList<>();
		collectInstalledUpgradesRecursive(component, upgrades);
		return upgrades;
	}

	/**
	 * Recursively collects all installed upgrades from a component and its nested structure parts.
	 */
	private void collectInstalledUpgradesRecursive(Component component, List<Component> upgrades) {
		// Collect direct upgrades
		if (component instanceof CustomizableComponent customizable) {
			upgrades.addAll(customizable.upgrades().filledContents());
		}

		// Recurse into structure parts
		if (component instanceof StructuredComponent structured) {
			for (ComponentPart part : structured.structure().allParts()) {
				collectInstalledUpgradesRecursive(part.content(), upgrades);
			}
		}
	}

	// ========== COMPATIBILITY CHECKING ==========

	@Override
	public boolean canInstall(Component target, Component upgrade) {
		return findCompatibleAnySlot(target, upgrade).isPresent();
	}

	/**
	 * Finds the first compatible empty slot of <em>any</em> Component-holding kind for an upgrade.
	 * Auto-routing ({@link #install}) uses this so a plugin slot kind is a valid destination; the
	 * default-kind result is unchanged because only {@link ComponentUpgradeSlot} (and opted-in
	 * plugin kinds) report {@link Slot#acceptsComponent()}.
	 */
	private Optional<Slot> findCompatibleAnySlot(Component target, Component upgrade) {
		return getAllSlots(target).stream()
				.filter(Slot::acceptsComponent)
				.filter(slot -> slot.componentContent().isEmpty())
				.filter(slot -> slot.validate(upgrade).isEmpty())
				.findFirst();
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

		Optional<Slot> compatibleSlot = findCompatibleAnySlot(target, upgrade);

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
		// Validate slot exists (any kind) and is empty
		Slot slot = getSlotById(target, slotId)
				.orElseThrow(() -> new IllegalArgumentException("Slot not found: " + slotId));

		if (slot.componentContent().isPresent()) {
			throw new IllegalArgumentException("Slot already filled: " + slotId +
					" (contains: " + slot.componentContent().map(Component::id).orElse(null) + ")");
		}

		// Validate upgrade against the slot (kind-generic compatibility seam)
		slot.validate(upgrade).ifPresent(error -> {
			throw new IllegalArgumentException(error);
		});

		return mutater.setSlot(target, slotId, upgrade);
	}

	@Override
	public Component installOrReplace(Component target, OpenIdentifier slotId, Component upgrade) {
		// Validate slot exists (any kind) and upgrade is compatible
		Slot slot = getSlotById(target, slotId)
				.orElseThrow(() -> new IllegalArgumentException("Slot not found: " + slotId));

		slot.validate(upgrade).ifPresent(error -> {
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
		// Get all filled slots recursively (including nested structure parts)
		List<ComponentUpgradeSlot> filledSlots = getFilledUpgradeSlots(target);

		if (filledSlots.isEmpty()) {
			return target;
		}

		// Remove each upgrade using the recursive mutater
		Component current = target;
		for (ComponentUpgradeSlot slot : filledSlots) {
			current = mutater.removeSlot(current, slot.id());
		}

		return current;
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
		return !getAllUpgradeSlots(component).isEmpty();
	}

	// ========== PRIVATE HELPER METHODS ==========

	/**
	 * Gets an upgrade slot by ID, searching recursively through structure parts.
	 *
	 * @param component The component to search
	 * @param slotId    The slot ID
	 * @return The upgrade slot if found
	 */
	/**
	 * Gets a slot of <em>any</em> kind by id, searching recursively through structure parts, so a
	 * plugin slot kind can be managed by id (install/replace/remove).
	 */
	private Optional<Slot> getSlotById(Component component, OpenIdentifier slotId) {
		if (component instanceof CustomizableComponent customizable) {
			Optional<Slot> directSlot = customizable.upgrades().getSlot(slotId);
			if (directSlot.isPresent()) {
				return directSlot;
			}
		}

		if (component instanceof StructuredComponent structured) {
			for (ComponentPart part : structured.structure().allParts()) {
				Optional<Slot> nestedSlot = getSlotById(part.content(), slotId);
				if (nestedSlot.isPresent()) {
					return nestedSlot;
				}
			}
		}

		return Optional.empty();
	}
}
