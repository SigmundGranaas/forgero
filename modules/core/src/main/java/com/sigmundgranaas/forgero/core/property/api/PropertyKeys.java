package com.sigmundgranaas.forgero.core.property.api;

import com.sigmundgranaas.forgero.core.attribute.api.Attribute;

/**
 * Registry of well-known property keys.
 * <p>
 * Use these constants for type-safe property access.
 * For custom properties, use {@link #custom(Class, String, String)}.
 * <p>
 * Example:
 * <pre>{@code
 * List<Attribute> attrs = component.properties(PropertyKeys.ATTRIBUTES);
 * }</pre>
 */
public final class PropertyKeys {

	private PropertyKeys() {
	}

	/**
	 * Attributes like damage, durability, speed.
	 */
	public static final PropertyKey<Attribute> ATTRIBUTES =
			new PropertyKey<>(Attribute.class, "forgero:attributes");

	/**
	 * Lore/description text lines.
	 */
	public static final PropertyKey<String> LORE =
			new PropertyKey<>(String.class, "forgero:lore");

	/**
	 * Generic feature flags (strings that identify enabled features).
	 */
	public static final PropertyKey<String> FEATURES =
			new PropertyKey<>(String.class, "forgero:features");

	/**
	 * Creates a custom property key for mod extensions.
	 * <p>
	 * Example:
	 * <pre>{@code
	 * PropertyKey<MagicProperty> MAGIC = PropertyKeys.custom(MagicProperty.class, "mymod", "magic");
	 * }</pre>
	 *
	 * @param type      The class of the property value
	 * @param namespace The mod namespace
	 * @param name      The property name
	 * @param <T>       The property value type
	 * @return A new PropertyKey instance
	 */
	public static <T> PropertyKey<T> custom(Class<T> type, String namespace, String name) {
		return new PropertyKey<>(type, namespace + ":" + name);
	}

	/**
	 * Creates a property key in the default (forgero) namespace.
	 *
	 * @param type The class of the property value
	 * @param name The property name
	 * @param <T>  The property value type
	 * @return A new PropertyKey instance
	 */
	public static <T> PropertyKey<T> forgero(Class<T> type, String name) {
		return custom(type, "forgero", name);
	}
}
