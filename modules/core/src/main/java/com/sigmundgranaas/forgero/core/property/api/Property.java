package com.sigmundgranaas.forgero.core.property.api;

/**
 * Marker interface for all property types that can be attached to components.
 * <p>
 * Properties represent data points that can be resolved from a component tree,
 * such as numerical attributes or behavioral features. This interface serves as
 * a common supertype for polymorphic property handling.
 * <p>
 * Implementations include:
 * <ul>
 *   <li>{@link com.sigmundgranaas.forgero.core.attribute.api.Attribute} - Numerical stats like attack damage, durability</li>
 *   <li>Feature properties - Behavioral modifications like enchantments, abilities</li>
 * </ul>
 *
 * @see PropertyKey for type-safe property access
 * @see PropertyHolder for components that contain properties
 */
public interface Property {
}
