package com.sigmundgranaas.forgero.core.component.api.slot;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Component;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * The optional upgrade slots on a component.
 * Slots may be empty or filled.
 *
 * @param slots The container holding all upgrade slots.
 */
public record ComponentUpgrades(SlotContainer<UpgradeSlot> slots) {

	/**
	 * Creates upgrades from varargs slots.
	 */
	public static ComponentUpgrades of(UpgradeSlot... slots) {
		return new ComponentUpgrades(SlotContainer.of(slots));
	}

	/**
	 * Creates upgrades from a collection of slots.
	 */
	public static ComponentUpgrades of(Collection<UpgradeSlot> slots) {
		return new ComponentUpgrades(SlotContainer.of(slots));
	}

	/**
	 * Creates empty upgrades (component has upgrade capability but no slots defined).
	 */
	public static ComponentUpgrades empty() {
		return new ComponentUpgrades(SlotContainer.empty());
	}

	/**
	 * Gets a slot by ID.
	 */
	public Optional<UpgradeSlot> get(OpenIdentifier id) {
		return slots.get(id);
	}

	/**
	 * Checks if a slot with the given ID exists.
	 */
	public boolean contains(OpenIdentifier id) {
		return slots.contains(id);
	}

	/**
	 * Returns all components currently filling upgrade slots.
	 * Empty slots are not included.
	 */
	public List<Component> filledContents() {
		return slots.all().stream()
				.flatMap(slot -> slot.content().stream())
				.toList();
	}

	/**
	 * Returns a new upgrades with the specified slot updated.
	 */
	public ComponentUpgrades withSlot(UpgradeSlot slot) {
		return new ComponentUpgrades(slots.with(slot));
	}

	/**
	 * Returns true if there are no upgrade slots.
	 */
	public boolean isEmpty() {
		return slots.isEmpty();
	}

	/**
	 * Returns the number of upgrade slots.
	 */
	public int size() {
		return slots.size();
	}

	/**
	 * Returns the number of filled upgrade slots.
	 */
	public int filledCount() {
		return (int) slots.all().stream().filter(UpgradeSlot::isFilled).count();
	}

	/**
	 * Returns true if all upgrade slots are filled.
	 */
	public boolean allFilled() {
		return slots.all().stream().allMatch(UpgradeSlot::isFilled);
	}

	/**
	 * Legacy compatibility: returns slots as a List.
	 *
	 * @deprecated Use {@link #slots()} and its methods instead
	 */
	@Deprecated
	public List<UpgradeSlot> slotsList() {
		return slots.asList();
	}
}
