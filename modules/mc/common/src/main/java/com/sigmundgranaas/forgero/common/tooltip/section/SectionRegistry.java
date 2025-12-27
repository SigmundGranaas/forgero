package com.sigmundgranaas.forgero.common.tooltip.section;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.tooltip.api.TooltipSection;
import com.sigmundgranaas.forgero.common.tooltip.api.writer.SectionWriterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central registry for tooltip sections.
 * <p>
 * Provides thread-safe registration and lookup of tooltip sections.
 * Supports priority overrides for custom section ordering.
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * // Via TooltipApi (recommended)
 * TooltipApi.registerSection(
 *     TooltipSection.forgero("description", 100),
 *     ctx -> new MyDescriptionWriter(ctx)
 * );
 *
 * // Direct registry access
 * SectionRegistry.register(section, factory);
 * SectionRegistry.setPriorityOverride(sectionId, 50);
 * }</pre>
 *
 * @see com.sigmundgranaas.forgero.common.tooltip.api.TooltipApi
 */
public final class SectionRegistry {
	private static final Logger LOGGER = LoggerFactory.getLogger(SectionRegistry.class);

	private static final Map<OpenIdentifier, RegisteredSection> SECTIONS = new ConcurrentHashMap<>();
	private static final Map<OpenIdentifier, Integer> PRIORITY_OVERRIDES = new ConcurrentHashMap<>();

	private SectionRegistry() {
	}

	/**
	 * Registers a new tooltip section with its writer factory.
	 *
	 * @param section The section definition
	 * @param factory The factory for creating section writers
	 */
	public static void register(TooltipSection section, SectionWriterFactory factory) {
		Objects.requireNonNull(section, "section cannot be null");
		Objects.requireNonNull(factory, "factory cannot be null");

		RegisteredSection registered = new RegisteredSection(section, factory);
		RegisteredSection existing = SECTIONS.putIfAbsent(section.id(), registered);

		if (existing != null) {
			LOGGER.warn("Tooltip section '{}' was already registered, ignoring duplicate", section.id());
		} else {
			LOGGER.debug("Registered tooltip section '{}' with priority {}", section.id(), section.priority());
		}
	}

	/**
	 * Registers or replaces a tooltip section.
	 *
	 * @param section The section definition
	 * @param factory The factory for creating section writers
	 */
	public static void registerOrReplace(TooltipSection section, SectionWriterFactory factory) {
		Objects.requireNonNull(section, "section cannot be null");
		Objects.requireNonNull(factory, "factory cannot be null");

		SECTIONS.put(section.id(), new RegisteredSection(section, factory));
		LOGGER.debug("Registered/replaced tooltip section '{}'", section.id());
	}

	/**
	 * Unregisters a tooltip section.
	 *
	 * @param sectionId The ID of the section to remove
	 * @return true if the section was removed
	 */
	public static boolean unregister(OpenIdentifier sectionId) {
		RegisteredSection removed = SECTIONS.remove(sectionId);
		if (removed != null) {
			LOGGER.debug("Unregistered tooltip section '{}'", sectionId);
			return true;
		}
		return false;
	}

	/**
	 * Gets a registered section by ID.
	 *
	 * @param id The section identifier
	 * @return The registered section if found
	 */
	public static Optional<RegisteredSection> get(OpenIdentifier id) {
		return Optional.ofNullable(SECTIONS.get(id));
	}

	/**
	 * Gets a registered section by string ID.
	 *
	 * @param id The section identifier string
	 * @return The registered section if found
	 */
	public static Optional<RegisteredSection> get(String id) {
		return get(OpenIdentifier.parse(id));
	}

	/**
	 * Checks if a section is registered.
	 *
	 * @param id The section identifier
	 * @return true if the section is registered
	 */
	public static boolean isRegistered(OpenIdentifier id) {
		return SECTIONS.containsKey(id);
	}

	/**
	 * Gets all registered sections sorted by effective priority.
	 *
	 * @return Immutable list in priority order (lower = first)
	 */
	public static List<RegisteredSection> getAllSorted() {
		return SECTIONS.values().stream()
				.sorted(Comparator.comparingInt(rs -> getEffectivePriority(rs.section().id())))
				.toList();
	}

	/**
	 * Gets all registered section IDs.
	 *
	 * @return Unmodifiable set of section IDs
	 */
	public static Set<OpenIdentifier> getAllIds() {
		return Collections.unmodifiableSet(SECTIONS.keySet());
	}

	/**
	 * Gets the total number of registered sections.
	 */
	public static int size() {
		return SECTIONS.size();
	}

	/**
	 * Sets a priority override for a section.
	 *
	 * @param sectionId The section to override
	 * @param priority  The new priority value (lower = first)
	 */
	public static void setPriorityOverride(OpenIdentifier sectionId, int priority) {
		PRIORITY_OVERRIDES.put(sectionId, priority);
		LOGGER.debug("Set priority override for '{}' to {}", sectionId, priority);
	}

	/**
	 * Clears a priority override for a section.
	 *
	 * @param sectionId The section to clear the override for
	 */
	public static void clearPriorityOverride(OpenIdentifier sectionId) {
		if (PRIORITY_OVERRIDES.remove(sectionId) != null) {
			LOGGER.debug("Cleared priority override for '{}'", sectionId);
		}
	}

	/**
	 * Clears all priority overrides.
	 */
	public static void clearAllPriorityOverrides() {
		PRIORITY_OVERRIDES.clear();
	}

	/**
	 * Gets the effective priority for a section.
	 *
	 * @param sectionId The section identifier
	 * @return The effective priority (override if set, otherwise default)
	 */
	public static int getEffectivePriority(OpenIdentifier sectionId) {
		Integer override = PRIORITY_OVERRIDES.get(sectionId);
		if (override != null) {
			return override;
		}
		RegisteredSection registered = SECTIONS.get(sectionId);
		return registered != null ? registered.section().priority() : Integer.MAX_VALUE;
	}

	/**
	 * Checks if a section has a priority override.
	 */
	public static boolean hasPriorityOverride(OpenIdentifier sectionId) {
		return PRIORITY_OVERRIDES.containsKey(sectionId);
	}

	/**
	 * Clears all registrations. For testing only.
	 */
	public static void clear() {
		SECTIONS.clear();
		PRIORITY_OVERRIDES.clear();
	}

	/**
	 * A registered section with its associated writer factory.
	 *
	 * @param section The section definition
	 * @param factory The factory for creating section writers
	 */
	public record RegisteredSection(TooltipSection section, SectionWriterFactory factory) {

		/**
		 * Gets the effective priority for this section.
		 */
		public int effectivePriority() {
			return getEffectivePriority(section.id());
		}
	}
}
