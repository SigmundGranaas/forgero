package com.sigmundgranaas.forgero.core.status.api;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * Global registry for StatusModifierDefinitions.
 * Loaded from JSON data packs during initialization.
 *
 * This registry holds all known status modifiers and provides
 * lookup and filtering capabilities for the modifier system.
 */
public final class StatusModifierRegistry {

	private static final Map<OpenIdentifier, StatusModifierDefinition> DEFINITIONS =
			new ConcurrentHashMap<>();

	// Well-known modifier IDs
	public static final OpenIdentifier BROKEN_ID = OpenIdentifier.of("broken");
	public static final OpenIdentifier UNBREAKABLE_ID = OpenIdentifier.of("unbreakable");

	private StatusModifierRegistry() {
		// Static utility class
	}

	// Registration methods

	/**
	 * Registers a status modifier definition.
	 *
	 * @param definition The definition to register
	 * @throws IllegalArgumentException if a definition with the same ID is already registered
	 */
	public static void register(StatusModifierDefinition definition) {
		OpenIdentifier id = definition.modifier().id();
		StatusModifierDefinition existing = DEFINITIONS.putIfAbsent(id, definition);
		if (existing != null) {
			throw new IllegalArgumentException("Status modifier already registered: " + id);
		}
	}

	/**
	 * Registers or replaces a status modifier definition.
	 * Use this for data pack overrides.
	 *
	 * @param definition The definition to register
	 */
	public static void registerOrReplace(StatusModifierDefinition definition) {
		DEFINITIONS.put(definition.modifier().id(), definition);
	}

	// Query methods

	/**
	 * Gets a definition by ID.
	 *
	 * @param id The modifier ID
	 * @return Optional containing the definition if found
	 */
	public static Optional<StatusModifierDefinition> get(OpenIdentifier id) {
		return Optional.ofNullable(DEFINITIONS.get(id));
	}

	/**
	 * Gets a modifier by ID.
	 *
	 * @param id The modifier ID
	 * @return Optional containing the modifier if found
	 */
	public static Optional<StatusModifier> getModifier(OpenIdentifier id) {
		return get(id).map(StatusModifierDefinition::modifier);
	}

	/**
	 * Gets a modifier by string ID.
	 *
	 * @param id The modifier ID string (e.g., "forgero:sharp")
	 * @return Optional containing the modifier if found
	 */
	public static Optional<StatusModifier> getModifier(String id) {
		return OpenIdentifier.tryParse(id).flatMap(StatusModifierRegistry::getModifier);
	}

	/**
	 * Checks if a modifier is registered.
	 *
	 * @param id The modifier ID
	 * @return true if registered
	 */
	public static boolean isRegistered(OpenIdentifier id) {
		return DEFINITIONS.containsKey(id);
	}

	/**
	 * Returns all registered definitions.
	 */
	public static Stream<StatusModifierDefinition> all() {
		return DEFINITIONS.values().stream();
	}

	/**
	 * Returns all registered modifiers.
	 */
	public static Stream<StatusModifier> allModifiers() {
		return all().map(StatusModifierDefinition::modifier);
	}

	/**
	 * Returns all registered modifier IDs.
	 */
	public static Set<OpenIdentifier> allIds() {
		return Collections.unmodifiableSet(DEFINITIONS.keySet());
	}

	/**
	 * Returns the number of registered modifiers.
	 */
	public static int size() {
		return DEFINITIONS.size();
	}

	// Filtering methods

	/**
	 * Finds all modifiers applicable to a component with the given tags.
	 *
	 * @param componentTags The tags of the target component
	 * @return List of applicable definitions
	 */
	public static List<StatusModifierDefinition> findApplicable(Set<OpenIdentifier> componentTags) {
		return all()
				.filter(def -> def.isApplicableTo(componentTags))
				.toList();
	}

	/**
	 * Finds all modifiers applicable to a specific component ID.
	 *
	 * @param componentId The component ID
	 * @return List of applicable definitions
	 */
	public static List<StatusModifierDefinition> findApplicableToId(OpenIdentifier componentId) {
		return all()
				.filter(def -> def.isApplicableToId(componentId))
				.toList();
	}

	/**
	 * Finds modifiers that could randomly occur, filtered by component tags.
	 *
	 * @param componentTags The tags of the target component
	 * @return List of definitions with non-zero chance that apply to the tags
	 */
	public static List<StatusModifierDefinition> findRandomApplicable(Set<OpenIdentifier> componentTags) {
		return all()
				.filter(def -> def.chance() > 0)
				.filter(def -> def.isApplicableTo(componentTags))
				.toList();
	}

	/**
	 * Finds modifiers by priority range.
	 *
	 * @param minPriority Minimum priority (inclusive)
	 * @param maxPriority Maximum priority (inclusive)
	 * @return List of definitions within the priority range
	 */
	public static List<StatusModifierDefinition> findByPriorityRange(int minPriority, int maxPriority) {
		return all()
				.filter(def -> {
					int p = def.modifier().priority();
					return p >= minPriority && p <= maxPriority;
				})
				.toList();
	}

	// Lifecycle methods

	/**
	 * Clears all registered definitions.
	 * Called during reload to reset the registry.
	 */
	public static void clear() {
		DEFINITIONS.clear();
	}

	/**
	 * Refreshes the registry by clearing and preparing for new registrations.
	 * Equivalent to clear() but more semantically clear for reload scenarios.
	 */
	public static void refresh() {
		clear();
	}
}
