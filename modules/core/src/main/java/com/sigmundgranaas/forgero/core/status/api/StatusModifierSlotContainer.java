package com.sigmundgranaas.forgero.core.status.api;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Slot;
import com.sigmundgranaas.forgero.core.component.api.slot.SlotContainer;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Type-safe wrapper around SlotContainer for status modifier slots.
 * Similar to ComponentUpgrades wrapper pattern.
 *
 * Provides convenient access to status modifier operations while
 * maintaining compatibility with the heterogeneous SlotContainer.
 */
public record StatusModifierSlotContainer(SlotContainer slots) {

	/**
	 * Creates a container from varargs StatusModifierSlots.
	 */
	public static StatusModifierSlotContainer of(StatusModifierSlot... slots) {
		return new StatusModifierSlotContainer(SlotContainer.of(List.of(slots)));
	}

	/**
	 * Creates a container from a collection of StatusModifierSlots.
	 */
	public static StatusModifierSlotContainer of(Collection<StatusModifierSlot> slots) {
		return new StatusModifierSlotContainer(SlotContainer.of(slots));
	}

	/**
	 * Creates an empty container.
	 */
	public static StatusModifierSlotContainer empty() {
		return new StatusModifierSlotContainer(SlotContainer.empty());
	}

	/**
	 * Creates a container with N empty slots.
	 *
	 * @param capacity Number of empty slots to create
	 * @return A container with the specified number of empty slots
	 */
	public static StatusModifierSlotContainer withCapacity(int capacity) {
		List<StatusModifierSlot> slots = new ArrayList<>(capacity);
		for (int i = 0; i < capacity; i++) {
			slots.add(StatusModifierSlot.emptyAt(i));
		}
		return of(slots);
	}

	// Query methods

	/**
	 * Gets a status modifier slot by ID.
	 */
	public Optional<StatusModifierSlot> get(OpenIdentifier id) {
		return slots.get(id)
				.filter(s -> s instanceof StatusModifierSlot)
				.map(s -> (StatusModifierSlot) s);
	}

	/**
	 * Gets a status modifier slot by index.
	 */
	public Optional<StatusModifierSlot> getByIndex(int index) {
		return statusSlots()
				.filter(s -> s.index() == index)
				.findFirst();
	}

	/**
	 * Returns all modifiers currently installed (from filled slots).
	 */
	public List<StatusModifier> allModifiers() {
		return statusSlots()
				.flatMap(slot -> slot.content().stream())
				.toList();
	}

	/**
	 * Returns all modifiers sorted by priority (highest first).
	 */
	public List<StatusModifier> modifiersByPriority() {
		return statusSlots()
				.flatMap(slot -> slot.content().stream())
				.sorted(Comparator.comparingInt(StatusModifier::priority).reversed())
				.toList();
	}

	/**
	 * Checks if a specific modifier is installed.
	 */
	public boolean hasModifier(OpenIdentifier modifierId) {
		return statusSlots()
				.flatMap(slot -> slot.content().stream())
				.anyMatch(m -> m.id().equals(modifierId));
	}

	/**
	 * Finds a modifier by ID if installed.
	 */
	public Optional<StatusModifier> findModifier(OpenIdentifier modifierId) {
		return statusSlots()
				.flatMap(slot -> slot.content().stream())
				.filter(m -> m.id().equals(modifierId))
				.findFirst();
	}

	/**
	 * Returns all status modifier slots.
	 */
	public List<StatusModifierSlot> allSlots() {
		return statusSlots().toList();
	}

	/**
	 * Alias for {@link #allSlots()} for consistency with interface patterns.
	 * Use this instead of the record component {@link #slots()} which returns SlotContainer.
	 */
	public List<StatusModifierSlot> statusSlotsList() {
		return allSlots();
	}

	/**
	 * Returns all empty status modifier slots.
	 */
	public List<StatusModifierSlot> emptySlots() {
		return statusSlots()
				.filter(StatusModifierSlot::isEmpty)
				.toList();
	}

	/**
	 * Returns all filled status modifier slots.
	 */
	public List<StatusModifierSlot> filledSlots() {
		return statusSlots()
				.filter(StatusModifierSlot::isFilled)
				.toList();
	}

	/**
	 * Finds the first empty slot.
	 */
	public Optional<StatusModifierSlot> firstEmpty() {
		return statusSlots()
				.filter(StatusModifierSlot::isEmpty)
				.findFirst();
	}

	/**
	 * @return Total number of status modifier slots
	 */
	public int size() {
		return (int) statusSlots().count();
	}

	/**
	 * @return Number of filled slots
	 */
	public int filledCount() {
		return (int) statusSlots().filter(StatusModifierSlot::isFilled).count();
	}

	/**
	 * @return Number of empty slots
	 */
	public int emptyCount() {
		return (int) statusSlots().filter(StatusModifierSlot::isEmpty).count();
	}

	/**
	 * @return true if all slots are empty
	 */
	public boolean isEmpty() {
		return statusSlots().allMatch(StatusModifierSlot::isEmpty);
	}

	/**
	 * @return true if all slots are filled
	 */
	public boolean isFull() {
		return statusSlots().allMatch(StatusModifierSlot::isFilled);
	}

	// Transformation methods

	/**
	 * Returns a new container with the specified slot replaced.
	 */
	public StatusModifierSlotContainer withSlot(StatusModifierSlot slot) {
		return new StatusModifierSlotContainer(slots.with(slot));
	}

	/**
	 * Returns a new container with a modifier installed in the first empty slot.
	 *
	 * @param modifier The modifier to install
	 * @return A new container with the modifier installed, or this if no empty slots
	 */
	public StatusModifierSlotContainer withModifier(StatusModifier modifier) {
		return firstEmpty()
				.map(slot -> withSlot(slot.withModifier(modifier)))
				.orElse(this);
	}

	/**
	 * Returns a new container with the specified modifier removed.
	 * The slot is emptied but preserved.
	 */
	public StatusModifierSlotContainer removeModifier(OpenIdentifier modifierId) {
		List<Slot> newSlots = slots.all().stream()
				.map(slot -> {
					if (slot instanceof StatusModifierSlot sms) {
						if (sms.content().map(m -> m.id().equals(modifierId)).orElse(false)) {
							return sms.empty();
						}
					}
					return slot;
				})
				.toList();
		return new StatusModifierSlotContainer(SlotContainer.of(newSlots));
	}

	/**
	 * Returns a new container with all slots emptied.
	 */
	public StatusModifierSlotContainer clearAll() {
		List<Slot> clearedSlots = slots.all().stream()
				.map(slot -> {
					if (slot instanceof StatusModifierSlot sms) {
						return sms.empty();
					}
					return slot;
				})
				.toList();
		return new StatusModifierSlotContainer(SlotContainer.of(clearedSlots));
	}

	/**
	 * Checks if a modifier can be installed (no incompatibilities with existing modifiers).
	 *
	 * @param modifier The modifier to check
	 * @return true if the modifier is compatible with all existing modifiers
	 */
	public boolean canInstall(StatusModifier modifier) {
		return !modifier.isIncompatibleWithAny(allModifiers()) && !hasModifier(modifier.id());
	}

	/**
	 * Attempts to install a modifier if compatible.
	 *
	 * @param modifier The modifier to install
	 * @return Optional containing the new container if successful, empty if incompatible or no slots
	 */
	public Optional<StatusModifierSlotContainer> tryInstall(StatusModifier modifier) {
		if (!canInstall(modifier)) {
			return Optional.empty();
		}
		return firstEmpty()
				.map(slot -> withSlot(slot.withModifier(modifier)));
	}

	// Helper method

	private Stream<StatusModifierSlot> statusSlots() {
		return slots.all().stream()
				.filter(s -> s instanceof StatusModifierSlot)
				.map(s -> (StatusModifierSlot) s);
	}
}
