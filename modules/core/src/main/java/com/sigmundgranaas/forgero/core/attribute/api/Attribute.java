package com.sigmundgranaas.forgero.core.attribute.api;

import com.sigmundgranaas.forgero.core.attribute.api.operator.Operator;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.property.api.PropertyKey;
import com.sigmundgranaas.forgero.core.condition.api.Condition;

import java.util.Optional;

/**
 * Represents a numerical attribute that can be composed and resolved.
 *
 * <p>Attributes have two orthogonal concerns:</p>
 * <ul>
 *   <li><b>Scope</b> - WHERE/HOW the attribute participates in composition
 *       (e.g., part-composite, upgrade). Empty means default propagation.</li>
 *   <li><b>Condition</b> - WHEN the attribute applies based on runtime state
 *       (e.g., is_sneaking, is_raining). This is evaluated after composition.</li>
 * </ul>
 *
 * <h2>Creating Attributes</h2>
 *
 * Use the fluent builder for complex attributes:
 * <pre>
 * // Material base value for part composition
 * Attribute durability = Attribute.of(DURABILITY, 240)
 *     .forPartComposition()
 *     .build();
 *
 * // Upgrade bonus with condition
 * Attribute sneakBonus = Attribute.of(ATTACK_DAMAGE, 5)
 *     .forUpgradeSlots()
 *     .when(Condition.isSneaking())
 *     .build();
 * </pre>
 *
 * Or use convenience methods for simple cases:
 * <pre>
 * Attribute simple = Attribute.simple(DURABILITY, 100);
 * </pre>
 *
 * @see AttributeScope for built-in scope types
 * @see AttributeBuilder for fluent construction
 */
public sealed interface Attribute permits SimpleAttribute {
	PropertyKey<Attribute> KEY = new PropertyKey<>(Attribute.class, "forgero:attributes");

	Optional<OpenIdentifier> id();
	OpenIdentifier type();
	float value();
	Operator operator();
	int group();

	/**
	 * The scope in which this attribute participates in composition.
	 *
	 * <p>Empty means this is a default attribute that propagates without
	 * special composition handling.</p>
	 *
	 * @return The composition scope, or empty for default behavior
	 */
	Optional<OpenIdentifier> scope();

	/**
	 * Runtime conditions that determine when this attribute applies.
	 *
	 * <p>Conditions are evaluated AFTER composition, during resolution.</p>
	 *
	 * @return The condition, or empty if always applies
	 */
	Optional<Condition> condition();

	// ========================================================================
	// FACTORY METHODS
	// ========================================================================

	/**
	 * Creates a builder for constructing attributes with clear intent.
	 *
	 * <pre>
	 * // Simple attribute
	 * Attribute.of(DURABILITY, 240).build();
	 *
	 * // Part composition (shape + material)
	 * Attribute.of(DURABILITY, 240)
	 *     .forPartComposition()
	 *     .build();
	 *
	 * // Upgrade bonus with condition
	 * Attribute.of(ATTACK_DAMAGE, 5)
	 *     .forUpgradeSlots()
	 *     .when(Condition.isSneaking())
	 *     .build();
	 * </pre>
	 *
	 * @param type The attribute type (e.g., forgero:durability)
	 * @param value The numerical value
	 * @return A builder for fluent construction
	 */
	static AttributeBuilder of(OpenIdentifier type, float value) {
		return new AttributeBuilder(type, value);
	}

	/**
	 * Creates a simple attribute with default scope and no conditions.
	 *
	 * <p>This is a convenience method for attributes that don't need
	 * special composition handling or runtime conditions.</p>
	 *
	 * @param type The attribute type
	 * @param value The numerical value
	 * @return A simple attribute with addition operator
	 */
	static Attribute simple(OpenIdentifier type, float value) {
		return new SimpleAttribute(type, value);
	}
}
