package com.sigmundgranaas.forgero.core.attribute.api;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;

/**
 * Defines the scope in which an attribute participates in composition.
 *
 * <p>Scope determines WHERE and HOW an attribute participates in composition,
 * NOT when it applies (that's what conditions are for).</p>
 *
 * <h3>Built-in Scopes:</h3>
 * <ul>
 *   <li>{@link #LOCAL} - Only applies to the component itself, does not propagate</li>
 *   <li>{@link #PART_COMPOSITE} - Participates in shape+material composition for parts</li>
 *   <li>{@link #EQUIPMENT_COMPOSITE} - Participates in parts→equipment composition</li>
 *   <li>{@link #UPGRADE} - Applied only when installed as an upgrade</li>
 * </ul>
 *
 * <p>An attribute with no scope (empty Optional) is a "default" attribute that
 * propagates normally without special composition handling.</p>
 *
 * <h3>Example JSON:</h3>
 * <pre>
 * {
 *   "type": "forgero:mining_speed",
 *   "scope": "forgero:scope/part-composite",
 *   "computation": { "multiply": 1.2 }
 * }
 * </pre>
 *
 * @see Attribute#scope()
 */
public final class AttributeScope {

	/**
	 * Attribute only applies to the component it's defined on.
	 *
	 * <p>Local attributes do NOT propagate to parent components during composition.
	 * Use this for attributes that should only affect the immediate component,
	 * like internal modifiers or component-specific bonuses.</p>
	 *
	 * <p>Example: A material's "crafting bonus" that only matters when
	 * the material itself is being processed, not when it's part of a tool.</p>
	 */
	public static final OpenIdentifier LOCAL = new OpenIdentifier("forgero", "scope/local");

	/**
	 * Participates in shape+material composition when building parts.
	 *
	 * <p>Attributes with this scope from shape and material are composed together
	 * using intersection logic: only attribute types present in BOTH sources
	 * (with at least one base and one multiplier) produce output.</p>
	 */
	public static final OpenIdentifier PART_COMPOSITE = new OpenIdentifier("forgero", "scope/part-composite");

	/**
	 * Participates in parts→equipment composition.
	 *
	 * <p>Used when combining multiple parts (head, handle, etc.) into equipment.</p>
	 */
	public static final OpenIdentifier EQUIPMENT_COMPOSITE = new OpenIdentifier("forgero", "scope/equipment-composite");

	/**
	 * Applied only when the component is installed as an upgrade.
	 *
	 * <p>Upgrade scope attributes are filtered out unless the component
	 * is placed in an upgrade slot. This prevents upgrade bonuses from
	 * applying when the component is used as a primary material.</p>
	 *
	 * <p>Example: A gem's "socket bonus" that only applies when the gem
	 * is installed in a tool's upgrade slot, not when used as crafting material.</p>
	 */
	public static final OpenIdentifier UPGRADE = new OpenIdentifier("forgero", "scope/upgrade");

	private AttributeScope() {
		// Utility class
	}

	// ========================================================================
	// CONVENIENCE METHODS
	// ========================================================================

	/**
	 * Checks if the given identifier is the part-composite scope.
	 *
	 * @param scope The scope to check
	 * @return true if this is the part-composite scope
	 */
	public static boolean isPartComposite(OpenIdentifier scope) {
		return PART_COMPOSITE.equals(scope);
	}

	/**
	 * Checks if the given identifier is the equipment-composite scope.
	 *
	 * @param scope The scope to check
	 * @return true if this is the equipment-composite scope
	 */
	public static boolean isEquipmentComposite(OpenIdentifier scope) {
		return EQUIPMENT_COMPOSITE.equals(scope);
	}

	/**
	 * Checks if the given identifier is the upgrade scope.
	 *
	 * @param scope The scope to check
	 * @return true if this is the upgrade scope
	 */
	public static boolean isUpgrade(OpenIdentifier scope) {
		return UPGRADE.equals(scope);
	}

	/**
	 * Checks if the given identifier is the local scope.
	 *
	 * @param scope The scope to check
	 * @return true if this is the local scope
	 */
	public static boolean isLocal(OpenIdentifier scope) {
		return LOCAL.equals(scope);
	}

	/**
	 * Checks if an Optional scope is empty or contains a known scope.
	 *
	 * @param scope The optional scope to check
	 * @return true if empty (no scope = default propagation)
	 */
	public static boolean isDefault(java.util.Optional<OpenIdentifier> scope) {
		return scope.isEmpty();
	}

	/**
	 * Gets the handler for the given scope identifier, if one exists.
	 *
	 * @param scope The scope identifier
	 * @return The handler for this scope, or empty if no handler exists
	 */
	public static java.util.Optional<com.sigmundgranaas.forgero.core.attribute.composition.AttributeScopeHandler> getHandler(OpenIdentifier scope) {
		if (PART_COMPOSITE.equals(scope)) {
			return java.util.Optional.of(com.sigmundgranaas.forgero.core.attribute.composition.PartCompositeScopeHandler.INSTANCE);
		}
		if (UPGRADE.equals(scope)) {
			return java.util.Optional.of(com.sigmundgranaas.forgero.core.attribute.composition.UpgradeScopeHandler.INSTANCE);
		}
		if (LOCAL.equals(scope)) {
			return java.util.Optional.of(com.sigmundgranaas.forgero.core.attribute.composition.LocalScopeHandler.INSTANCE);
		}
		return java.util.Optional.empty();
	}

	/**
	 * Checks if the given scope is a composite scope (requires composition handling).
	 */
	public static boolean isCompositionScope(OpenIdentifier scope) {
		return PART_COMPOSITE.equals(scope) || EQUIPMENT_COMPOSITE.equals(scope);
	}

	/**
	 * Checks if the given scope is a filter scope (simple include/exclude).
	 */
	public static boolean isFilterScope(OpenIdentifier scope) {
		return LOCAL.equals(scope) || UPGRADE.equals(scope);
	}

	/**
	 * Checks if an attribute's scope matches a slot's scope for upgrade filtering.
	 * Uses simple equality matching without tag hierarchy resolution.
	 *
	 * <p>This method delegates to {@link ScopeMatchDecisionTable} which provides
	 * a data-driven, rule-based implementation that is easier to test, debug,
	 * and extend.</p>
	 *
	 * <p>Matching rules:</p>
	 * <ul>
	 *   <li>Attribute with no scope → matches any slot (default behavior)</li>
	 *   <li>Attribute with {@link #UPGRADE} scope → matches any upgrade slot</li>
	 *   <li>Slot with no scope → only no-scope and UPGRADE attributes pass</li>
	 *   <li>Slot with specific scope → matches attributes with exact scope match</li>
	 * </ul>
	 *
	 * <p>IMPORTANT: Attributes with {@link #PART_COMPOSITE} or {@link #EQUIPMENT_COMPOSITE}
	 * scopes will NOT match upgrade slots. These scopes are for part/equipment composition
	 * only, not for upgrade bonus application.</p>
	 *
	 * @param attributeScope The attribute's scope (may be empty for default attributes)
	 * @param slotScope The slot's scope (may be empty for unfiltered slots)
	 * @return true if the attribute should be included for this slot
	 * @see ScopeMatchDecisionTable#matches(java.util.Optional, java.util.Optional)
	 */
	public static boolean matchesSlotScope(java.util.Optional<OpenIdentifier> attributeScope, java.util.Optional<OpenIdentifier> slotScope) {
		return ScopeMatchDecisionTable.matches(attributeScope, slotScope);
	}

	/**
	 * Checks if an attribute's scope matches a slot's scope using tag hierarchy resolution.
	 *
	 * <p>This method delegates to {@link ScopeMatchDecisionTable} which provides
	 * a data-driven, rule-based implementation that is easier to test, debug,
	 * and extend.</p>
	 *
	 * <p>Matching rules:</p>
	 * <ul>
	 *   <li>Attribute with no scope → matches any slot (default behavior)</li>
	 *   <li>Attribute with {@link #UPGRADE} scope → matches any upgrade slot</li>
	 *   <li>Attribute scope matches if equal to slot scope OR is a descendant of slot scope</li>
	 * </ul>
	 *
	 * <p>Example: Slot with scope "forgero:offensive" matches attributes with scope
	 * "forgero:offensive" or any child scope like "forgero:melee_offensive".</p>
	 *
	 * @param attributeScope The attribute's scope (may be empty for default attributes)
	 * @param slotScope The slot's scope (may be empty for unfiltered slots)
	 * @param tagResolver The tag resolver for checking tag hierarchy relationships
	 * @return true if the attribute should be included for this slot
	 * @see ScopeMatchDecisionTable#matches(java.util.Optional, java.util.Optional, com.sigmundgranaas.forgero.common.tags.api.TagResolver)
	 */
	public static boolean matchesSlotScope(
			java.util.Optional<OpenIdentifier> attributeScope,
			java.util.Optional<OpenIdentifier> slotScope,
			com.sigmundgranaas.forgero.common.tags.api.TagResolver tagResolver
	) {
		return ScopeMatchDecisionTable.matches(attributeScope, slotScope, tagResolver);
	}
}
