package com.sigmundgranaas.forgero.common.tooltip.value;

import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.property.context.DynamicContext;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

/**
 * Registry for custom placeholder resolvers.
 * <p>
 * Allows mods to register custom placeholders beyond standard attributes.
 * Placeholders are resolved by name when processing tooltip templates.
 *
 * <h2>Built-in Placeholders</h2>
 * <ul>
 *   <li>{@code material_name} - The name part of the component's identifier</li>
 *   <li>{@code namespace} - The namespace of the component's identifier</li>
 *   <li>{@code full_id} - The full identifier string</li>
 * </ul>
 *
 * <h2>Registering Custom Placeholders</h2>
 * <pre>{@code
 * PlaceholderRegistry.register("rarity", (component, context) ->
 *     Optional.of(RarityHelper.getRarity(component).name()));
 *
 * PlaceholderRegistry.register("enchantment_level", (component, context) ->
 *     context.get(EnchantmentKeys.LEVEL).map(l -> (Object) l));
 * }</pre>
 */
public final class PlaceholderRegistry {

	private static final Map<String, PlaceholderResolver> RESOLVERS = new ConcurrentHashMap<>();

	private PlaceholderRegistry() {
	}

	/**
	 * Functional interface for placeholder resolution.
	 */
	@FunctionalInterface
	public interface PlaceholderResolver extends BiFunction<Component, DynamicContext, Optional<Object>> {
	}

	/**
	 * Registers a custom placeholder resolver.
	 * <p>
	 * Placeholder names are case-insensitive during lookup.
	 *
	 * @param name     The placeholder name (e.g., "rarity")
	 * @param resolver The function to resolve the value
	 */
	public static void register(String name, PlaceholderResolver resolver) {
		RESOLVERS.put(name.toLowerCase(), resolver);
	}

	/**
	 * Unregisters a placeholder.
	 *
	 * @param name The placeholder name
	 * @return true if the placeholder was registered
	 */
	public static boolean unregister(String name) {
		return RESOLVERS.remove(name.toLowerCase()) != null;
	}

	/**
	 * Checks if a placeholder is registered.
	 *
	 * @param name The placeholder name
	 * @return true if registered
	 */
	public static boolean isRegistered(String name) {
		return RESOLVERS.containsKey(name.toLowerCase());
	}

	/**
	 * Resolves a placeholder to its value.
	 *
	 * @param name      The placeholder name
	 * @param component The component being rendered
	 * @param context   The dynamic context
	 * @return The resolved value, or empty if not registered or resolver returns empty
	 */
	public static Optional<Object> resolve(String name, Component component, DynamicContext context) {
		PlaceholderResolver resolver = RESOLVERS.get(name.toLowerCase());
		if (resolver != null) {
			return resolver.apply(component, context);
		}
		return Optional.empty();
	}

	/**
	 * Clears all registered placeholders.
	 */
	public static void clear() {
		RESOLVERS.clear();
		registerDefaults();
	}

	/**
	 * Registers the default built-in placeholders.
	 */
	public static void registerDefaults() {
		// Component identifier parts
		register("material_name", (comp, ctx) ->
				Optional.of(comp.id().path()));

		register("namespace", (comp, ctx) ->
				Optional.of(comp.id().namespace()));

		register("full_id", (comp, ctx) ->
				Optional.of(comp.id().toString()));

		// Component name (path without underscores, title case)
		register("display_name", (comp, ctx) -> {
			String path = comp.id().path();
			String[] parts = path.split("_");
			StringBuilder sb = new StringBuilder();
			for (String part : parts) {
				if (!sb.isEmpty()) {
					sb.append(" ");
				}
				if (!part.isEmpty()) {
					sb.append(Character.toUpperCase(part.charAt(0)));
					if (part.length() > 1) {
						sb.append(part.substring(1).toLowerCase());
					}
				}
			}
			return Optional.of(sb.toString());
		});
	}

	// Initialize defaults on class load
	static {
		registerDefaults();
	}
}
