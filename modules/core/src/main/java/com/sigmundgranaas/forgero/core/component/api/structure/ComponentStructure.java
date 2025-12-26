package com.sigmundgranaas.forgero.core.component.api.structure;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.slot.SlotContainer;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The required structural composition of a component.
 * All slots in a structure must be filled - there are no empty structural slots.
 *
 * @param slots The container holding all structure slots.
 */
public record ComponentStructure(SlotContainer<StructureSlot> slots) {

	/**
	 * Creates a structure from varargs slots.
	 */
	public static ComponentStructure of(StructureSlot... slots) {
		return new ComponentStructure(SlotContainer.of(slots));
	}

	/**
	 * Creates a structure from a collection of slots.
	 */
	public static ComponentStructure of(Collection<StructureSlot> slots) {
		return new ComponentStructure(SlotContainer.of(slots));
	}

	/**
	 * Creates an empty structure (rare, but valid for some component types).
	 */
	public static ComponentStructure empty() {
		return new ComponentStructure(SlotContainer.empty());
	}

	/**
	 * Gets a slot by ID.
	 */
	public Optional<StructureSlot> get(OpenIdentifier id) {
		return slots.get(id);
	}

	/**
	 * Checks if a slot with the given ID exists.
	 */
	public boolean contains(OpenIdentifier id) {
		return slots.contains(id);
	}

	/**
	 * Returns all child components from all slots.
	 */
	public List<Component> children() {
		return slots.all().stream()
				.map(StructureSlot::content)
				.toList();
	}

	/**
	 * Returns a new structure with the specified slot updated.
	 */
	public ComponentStructure withSlot(StructureSlot slot) {
		return new ComponentStructure(slots.with(slot));
	}

	/**
	 * Returns true if this structure has no slots.
	 */
	public boolean isEmpty() {
		return slots.isEmpty();
	}

	/**
	 * Returns the number of slots.
	 */
	public int size() {
		return slots.size();
	}

	/**
	 * Legacy compatibility: returns slots as a Map.
	 *
	 * @deprecated Use {@link #slots()} and its methods instead
	 */
	@Deprecated
	public Map<OpenIdentifier, StructureSlot> slotsAsMap() {
		return slots.all().stream()
				.collect(Collectors.toMap(StructureSlot::id, s -> s));
	}
}
