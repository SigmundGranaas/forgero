package com.sigmundgranaas.forgero.common.tooltip.api;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;

/**
 * Immutable definition of a tooltip section.
 * <p>
 * Sections organize tooltip content into logical groups like description,
 * attributes, features, etc. Each section has:
 * <ul>
 *   <li>A unique identifier (e.g., "forgero:description")</li>
 *   <li>A priority for ordering (lower = appears first)</li>
 *   <li>An optional translation key for headers</li>
 * </ul>
 *
 * <h2>Creating Sections</h2>
 * <pre>{@code
 * // Section with header
 * TooltipSection attributes = TooltipSection.of("mymod:stats", 300, true);
 *
 * // Section without header (e.g., for subtitles)
 * TooltipSection subtitle = TooltipSection.of("mymod:subtitle", 0, false);
 *
 * // Using forgero namespace
 * TooltipSection desc = TooltipSection.forgero("description", 100);
 * }</pre>
 *
 * <h2>Default Sections</h2>
 * <p>Forgero provides these default sections (in priority order):
 * <ol>
 *   <li>subtitle (0) - Item subtitle, no header</li>
 *   <li>description (100) - Item description</li>
 *   <li>notes (200) - Additional notes</li>
 *   <li>attributes (300) - Attribute stats with comparison</li>
 *   <li>features (400) - Special features/abilities</li>
 *   <li>upgrades (500) - Applied upgrades</li>
 *   <li>slots (600) - Available upgrade slots</li>
 *   <li>lore (900) - Flavor text</li>
 * </ol>
 *
 * @param id             Unique identifier for this section
 * @param priority       Default ordering priority (lower = first)
 * @param translationKey Translation key for section header
 * @param showHeader     Whether to show the section header
 */
public record TooltipSection(
		OpenIdentifier id,
		int priority,
		String translationKey,
		boolean showHeader
) {

	/**
	 * Creates a section with an identifier string.
	 *
	 * @param id         The section ID (e.g., "mymod:custom")
	 * @param priority   The display priority (lower = first)
	 * @param showHeader Whether to show a header
	 * @return A new TooltipSection
	 */
	public static TooltipSection of(String id, int priority, boolean showHeader) {
		OpenIdentifier identifier = OpenIdentifier.parse(id);
		String translationKey = showHeader
				? "tooltip." + identifier.namespace() + ".section." + identifier.path()
				: "";
		return new TooltipSection(identifier, priority, translationKey, showHeader);
	}

	/**
	 * Creates a section with namespace and path.
	 *
	 * @param namespace  The namespace (e.g., "mymod")
	 * @param path       The section path (e.g., "custom")
	 * @param priority   The display priority (lower = first)
	 * @param showHeader Whether to show a header
	 * @return A new TooltipSection
	 */
	public static TooltipSection of(String namespace, String path, int priority, boolean showHeader) {
		OpenIdentifier id = new OpenIdentifier(namespace, path);
		String translationKey = showHeader
				? "tooltip." + namespace + ".section." + path
				: "";
		return new TooltipSection(id, priority, translationKey, showHeader);
	}

	/**
	 * Creates a section with a header using the forgero namespace.
	 *
	 * @param path     The section path (e.g., "description")
	 * @param priority The display priority (lower = first)
	 * @return A new TooltipSection with header enabled
	 */
	public static TooltipSection forgero(String path, int priority) {
		return of("forgero", path, priority, true);
	}

	/**
	 * Creates a headerless section using the forgero namespace.
	 *
	 * @param path     The section path (e.g., "subtitle")
	 * @param priority The display priority (lower = first)
	 * @return A new TooltipSection without header
	 */
	public static TooltipSection forgeroHeaderless(String path, int priority) {
		return of("forgero", path, priority, false);
	}

	/**
	 * Gets the simple path of this section (without namespace).
	 */
	public String path() {
		return id.path();
	}

	/**
	 * Gets the namespace of this section.
	 */
	public String namespace() {
		return id.namespace();
	}

	/**
	 * Creates a copy with a different priority.
	 *
	 * @param newPriority The new priority value
	 * @return A new TooltipSection with the updated priority
	 */
	public TooltipSection withPriority(int newPriority) {
		return new TooltipSection(id, newPriority, translationKey, showHeader);
	}
}
