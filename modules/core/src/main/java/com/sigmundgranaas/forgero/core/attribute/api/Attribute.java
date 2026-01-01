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
 *   <li><b>Context</b> - WHERE/HOW the attribute participates in composition
 *       (e.g., part-composite, upgrade). Empty means default propagation.</li>
 *   <li><b>Condition</b> - WHEN the attribute applies based on runtime state
 *       (e.g., is_sneaking, is_raining). This is evaluated after composition.</li>
 * </ul>
 *
 * @see AttributeContext for built-in context types
 */
public sealed interface Attribute permits SimpleAttribute {
	PropertyKey<Attribute> KEY = new PropertyKey<>(Attribute.class, "forgero:attributes");

	Optional<OpenIdentifier> id();
	OpenIdentifier type();
	float value();
	Operator operator();
	int group();

	/**
	 * The context in which this attribute participates in composition.
	 *
	 * <p>Empty means this is a default attribute that propagates without
	 * special composition handling.</p>
	 *
	 * @return The composition context, or empty for default behavior
	 */
	Optional<OpenIdentifier> context();

	/**
	 * Runtime conditions that determine when this attribute applies.
	 *
	 * <p>Conditions are evaluated AFTER composition, during resolution.</p>
	 *
	 * @return The condition, or empty if always applies
	 */
	Optional<Condition> condition();
}
