package com.sigmundgranaas.forgero.core.component.api.slot;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Slot;

import java.util.*;

/**
 * Container for heterogeneous mutable slots.
 * Holds different slot implementations (ComponentUpgradeSlot, ArrowSlot, SoulSlot, etc.).
 *
 * This is a final class - there's only one way to contain slots.
 * NOTE: This is for MUTABLE slots only. Immutable structure uses ComponentStructure with ComponentPart.
 */
public final class SlotContainer {
	private final Map<OpenIdentifier, Slot> slots;

	private SlotContainer(Map<OpenIdentifier, Slot> slots) {
		this.slots = Map.copyOf(slots);
	}

	/**
	 * Creates a container from a collection of slots.
	 *
	 * @throws IllegalArgumentException if duplicate slot IDs are found
	 */
	public static SlotContainer of(Collection<? extends Slot> slots) {
		Map<OpenIdentifier, Slot> map = new LinkedHashMap<>();
		for (Slot slot : slots) {
			if (map.containsKey(slot.id())) {
				throw new IllegalArgumentException("Duplicate slot ID: " + slot.id());
			}
			map.put(slot.id(), slot);
		}
		return new SlotContainer(map);
	}

	/**
	 * Creates a container from varargs slots.
	 */
	@SafeVarargs
	public static <S extends Slot> SlotContainer of(S... slots) {
		return of(List.of(slots));
	}

	/**
	 * Creates an empty container.
	 */
	public static SlotContainer empty() {
		return new SlotContainer(Map.of());
	}

	/**
	 * Gets a slot by ID.
	 */
	public Optional<Slot> get(OpenIdentifier id) {
		return Optional.ofNullable(slots.get(id));
	}

	/**
	 * Checks if a slot with the given ID exists.
	 */
	public boolean contains(OpenIdentifier id) {
		return slots.containsKey(id);
	}

	/**
	 * Returns all slots in insertion order.
	 */
	public Collection<Slot> all() {
		return slots.values();
	}

	/**
	 * Returns all slot IDs.
	 */
	public Set<OpenIdentifier> ids() {
		return slots.keySet();
	}

	/**
	 * Returns the number of slots.
	 */
	public int size() {
		return slots.size();
	}

	/**
	 * Checks if the container is empty.
	 */
	public boolean isEmpty() {
		return slots.isEmpty();
	}

	/**
	 * Returns a new container with the specified slot replaced or added.
	 */
	public SlotContainer with(Slot slot) {
		Map<OpenIdentifier, Slot> newSlots = new LinkedHashMap<>(slots);
		newSlots.put(slot.id(), slot);
		return new SlotContainer(newSlots);
	}

	/**
	 * Returns a new container with the specified slot removed.
	 */
	public SlotContainer without(OpenIdentifier id) {
		if (!slots.containsKey(id)) {
			return this;
		}
		Map<OpenIdentifier, Slot> newSlots = new LinkedHashMap<>(slots);
		newSlots.remove(id);
		return new SlotContainer(newSlots);
	}

	/**
	 * Returns the slots as an unmodifiable list (for backwards compatibility).
	 */
	public List<Slot> asList() {
		return List.copyOf(slots.values());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof SlotContainer that)) return false;
		return slots.equals(that.slots);
	}

	@Override
	public int hashCode() {
		return slots.hashCode();
	}

	@Override
	public String toString() {
		return "SlotContainer{" + slots.keySet() + "}";
	}
}
