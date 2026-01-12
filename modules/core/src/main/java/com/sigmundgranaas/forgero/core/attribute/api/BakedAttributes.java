package com.sigmundgranaas.forgero.core.attribute.api;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;

import java.util.Map;

/**
 * Immutable result of the bake phase, organized by attribute type for O(1) lookup.
 *
 * <p>This structure replaces the previous {@code List<Attribute>} approach which
 * required O(n) iteration per query. With this map-based structure:
 * <ul>
 *   <li>Lookups by attribute type are O(1)</li>
 *   <li>Base values are pre-computed during bake (unconditional attributes)</li>
 *   <li>Only conditional attributes require runtime evaluation</li>
 * </ul>
 *
 * <h3>Example:</h3>
 * <pre>
 * // During bake phase, an iron pickaxe produces:
 * BakedAttributes(
 *     byType: {
 *         "forgero:attack_damage" → PrecomputedAttribute(7.0, [+3 if sneaking]),
 *         "forgero:durability"    → PrecomputedAttribute(1200, []),
 *         "forgero:mining_speed"  → PrecomputedAttribute(6.0, [+2 if raining])
 *     }
 * )
 *
 * // During apply phase:
 * baked.get(attackDamageId).compute(context)  // O(1) lookup + conditional eval
 * </pre>
 *
 * @param byType Map from attribute type identifier to its precomputed value
 */
public record BakedAttributes(
		Map<OpenIdentifier, PrecomputedAttribute> byType
) {
	/**
	 * Empty baked attributes with no entries.
	 * Used when a component has no attributes.
	 */
	public static final BakedAttributes EMPTY = new BakedAttributes(Map.of());

	/**
	 * Defensive copy constructor to ensure immutability.
	 */
	public BakedAttributes {
		byType = Map.copyOf(byType);
	}

	/**
	 * Gets the precomputed attribute for a specific type.
	 *
	 * @param type The attribute type identifier
	 * @return The precomputed attribute, or {@link PrecomputedAttribute#ZERO} if not present
	 */
	public PrecomputedAttribute get(OpenIdentifier type) {
		return byType.getOrDefault(type, PrecomputedAttribute.ZERO);
	}

	/**
	 * Returns true if this contains no attributes.
	 */
	public boolean isEmpty() {
		return byType.isEmpty();
	}

	/**
	 * Returns the number of distinct attribute types.
	 */
	public int size() {
		return byType.size();
	}

	/**
	 * Returns true if this contains an attribute of the given type.
	 *
	 * @param type The attribute type to check for
	 * @return true if the type exists in this baked result
	 */
	public boolean contains(OpenIdentifier type) {
		return byType.containsKey(type);
	}
}
