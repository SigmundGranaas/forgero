package com.sigmundgranaas.forgero.core.component.api.structure;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Immutable component structure - collection of required ComponentParts.
 * NOT related to the mutable Slot system.
 *
 * All parts in a structure must be filled - there are no empty structure parts.
 * Parts define the immutable composition of a component (blade, handle, limbs, etc.).
 *
 * @param parts Map of part ID to ComponentPart. Immutable.
 */
public record ComponentStructure(Map<OpenIdentifier, ComponentPart> parts) {

	public ComponentStructure {
		// Ensure immutability
		parts = Collections.unmodifiableMap(new LinkedHashMap<>(parts));
	}

	/**
	 * Creates a structure from varargs parts.
	 * @throws IllegalArgumentException if duplicate part IDs are provided
	 */
	public static ComponentStructure of(ComponentPart... parts) {
		Map<OpenIdentifier, ComponentPart> map = new LinkedHashMap<>();
		for (ComponentPart part : parts) {
			if (map.containsKey(part.id())) {
				throw new IllegalArgumentException("Duplicate part ID: " + part.id());
			}
			map.put(part.id(), part);
		}
		return new ComponentStructure(map);
	}

	/**
	 * Creates a structure from a collection of parts.
	 * @throws IllegalArgumentException if duplicate part IDs are provided
	 */
	public static ComponentStructure of(Collection<ComponentPart> parts) {
		Map<OpenIdentifier, ComponentPart> map = new LinkedHashMap<>();
		for (ComponentPart part : parts) {
			if (map.containsKey(part.id())) {
				throw new IllegalArgumentException("Duplicate part ID: " + part.id());
			}
			map.put(part.id(), part);
		}
		return new ComponentStructure(map);
	}

	/**
	 * Creates an empty structure (rare, but valid for some component types).
	 */
	public static ComponentStructure empty() {
		return new ComponentStructure(Collections.emptyMap());
	}

	/**
	 * Gets all parts.
	 */
	public Collection<ComponentPart> allParts() {
		return parts.values();
	}

	/**
	 * Gets a part by ID.
	 */
	public Optional<ComponentPart> getPart(OpenIdentifier id) {
		return Optional.ofNullable(parts.get(id));
	}

	/**
	 * Checks if a part with the given ID exists.
	 */
	public boolean contains(OpenIdentifier id) {
		return parts.containsKey(id);
	}

	/**
	 * Returns all child components from all structure parts.
	 * All parts are always filled (never null).
	 */
	public List<Component> children() {
		return parts.values().stream()
				.map(ComponentPart::getContent)
				.toList();
	}

	/**
	 * Returns a new structure with the specified part updated.
	 */
	public ComponentStructure withPart(ComponentPart part) {
		Map<OpenIdentifier, ComponentPart> newParts = new LinkedHashMap<>(parts);
		newParts.put(part.id(), part);
		return new ComponentStructure(newParts);
	}

	/**
	 * Returns true if this structure has no parts.
	 */
	public boolean isEmpty() {
		return parts.isEmpty();
	}

	/**
	 * Returns the number of parts.
	 */
	public int size() {
		return parts.size();
	}
}
