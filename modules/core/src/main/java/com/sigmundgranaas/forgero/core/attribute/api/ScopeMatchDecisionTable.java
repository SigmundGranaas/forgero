package com.sigmundgranaas.forgero.core.attribute.api;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.tags.api.TagResolver;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Data-driven decision table for attribute scope to slot scope matching.
 *
 * <p>This class encapsulates the business rules for determining whether an attribute
 * should be included when filtering by slot scope. Rules are explicit, testable,
 * and easier to understand than the equivalent procedural if-statement chains.</p>
 *
 * <h2>Matching Rules (evaluated in order):</h2>
 * <table>
 *   <tr><th>Attribute Scope</th><th>Slot Scope</th><th>Result</th><th>Rationale</th></tr>
 *   <tr><td>Empty (default)</td><td>Any</td><td>MATCH</td><td>Default attributes propagate everywhere</td></tr>
 *   <tr><td>PART_COMPOSITE</td><td>Any</td><td>NO MATCH</td><td>Composition scope, not for upgrades</td></tr>
 *   <tr><td>EQUIPMENT_COMPOSITE</td><td>Any</td><td>NO MATCH</td><td>Composition scope, not for upgrades</td></tr>
 *   <tr><td>UPGRADE</td><td>Any</td><td>MATCH</td><td>Upgrade-scoped attrs apply in any upgrade slot</td></tr>
 *   <tr><td>LOCAL</td><td>Any</td><td>NO MATCH</td><td>Local scope doesn't propagate</td></tr>
 *   <tr><td>Custom</td><td>Empty</td><td>NO MATCH</td><td>Custom scopes require explicit slot filter</td></tr>
 *   <tr><td>Custom</td><td>Same as attr</td><td>MATCH</td><td>Exact scope match</td></tr>
 *   <tr><td>Custom</td><td>Different</td><td>NO MATCH</td><td>Scope mismatch</td></tr>
 * </table>
 *
 * @see AttributeScope#matchesSlotScope(Optional, Optional)
 */
public final class ScopeMatchDecisionTable {

	/**
	 * Built-in composition scopes that are never valid for upgrade slots.
	 */
	private static final Set<OpenIdentifier> COMPOSITION_SCOPES = Set.of(
			AttributeScope.PART_COMPOSITE,
			AttributeScope.EQUIPMENT_COMPOSITE
	);

	/**
	 * All built-in scopes that have special handling.
	 */
	private static final Set<OpenIdentifier> ALL_BUILT_IN_SCOPES = Set.of(
			AttributeScope.PART_COMPOSITE,
			AttributeScope.EQUIPMENT_COMPOSITE,
			AttributeScope.LOCAL,
			AttributeScope.UPGRADE
	);

	/**
	 * The decision table - rules evaluated in order.
	 * First matching rule determines the result.
	 */
	private static final List<ScopeMatchRule> RULES = List.of(
			// Rule 1: Default (no-scope) attributes always match
			new ScopeMatchRule(
					new ScopeMatcher.Empty(),
					new ScopeMatcher.Any(),
					true,
					"Default attributes propagate everywhere"
			),

			// Rule 2 & 3: Composition scopes never match upgrade slots
			new ScopeMatchRule(
					new ScopeMatcher.AnyOf(COMPOSITION_SCOPES),
					new ScopeMatcher.Any(),
					false,
					"Composition scopes (PART_COMPOSITE, EQUIPMENT_COMPOSITE) are for part/equipment composition only"
			),

			// Rule 4: UPGRADE scope always matches any upgrade slot
			new ScopeMatchRule(
					new ScopeMatcher.Exact(AttributeScope.UPGRADE),
					new ScopeMatcher.Any(),
					true,
					"UPGRADE scope applies in any upgrade slot"
			),

			// Rule 5: LOCAL scope never propagates
			new ScopeMatchRule(
					new ScopeMatcher.Exact(AttributeScope.LOCAL),
					new ScopeMatcher.Any(),
					false,
					"LOCAL scope does not propagate to parent components"
			),

			// Rule 6: Custom scopes require slot to have a filter
			new ScopeMatchRule(
					new ScopeMatcher.CustomScope(ALL_BUILT_IN_SCOPES),
					new ScopeMatcher.Empty(),
					false,
					"Custom scopes require explicit slot scope filter"
			)

			// Rules 7 & 8: Custom scope exact match is handled by default fallback
			// (no explicit rule needed - exact equality check in matches())
	);

	private ScopeMatchDecisionTable() {
		// Utility class
	}

	/**
	 * Evaluates the decision table for the given scopes.
	 *
	 * @param attributeScope The attribute's scope (may be empty for default attributes)
	 * @param slotScope The slot's scope (may be empty for unfiltered slots)
	 * @return true if the attribute should be included for this slot
	 */
	public static boolean matches(Optional<OpenIdentifier> attributeScope, Optional<OpenIdentifier> slotScope) {
		// Evaluate rules in order
		for (ScopeMatchRule rule : RULES) {
			if (rule.applies(attributeScope, slotScope)) {
				return rule.result();
			}
		}

		// Default fallback: exact match required
		return attributeScope.equals(slotScope);
	}

	/**
	 * Evaluates the decision table with tag hierarchy support.
	 *
	 * <p>This extends the basic matching with tag hierarchy resolution:
	 * an attribute's scope matches if it equals the slot scope OR is a descendant
	 * of the slot scope in the tag hierarchy.</p>
	 *
	 * @param attributeScope The attribute's scope (may be empty for default attributes)
	 * @param slotScope The slot's scope (may be empty for unfiltered slots)
	 * @param tagResolver The tag resolver for checking tag hierarchy relationships
	 * @return true if the attribute should be included for this slot
	 */
	public static boolean matches(
			Optional<OpenIdentifier> attributeScope,
			Optional<OpenIdentifier> slotScope,
			TagResolver tagResolver
	) {
		// Evaluate rules in order
		for (ScopeMatchRule rule : RULES) {
			if (rule.applies(attributeScope, slotScope)) {
				return rule.result();
			}
		}

		// Default fallback with hierarchy support
		if (attributeScope.isEmpty() || slotScope.isEmpty()) {
			return attributeScope.equals(slotScope);
		}

		OpenIdentifier attrScope = attributeScope.get();
		OpenIdentifier slotScopeId = slotScope.get();

		// Exact match
		if (attrScope.equals(slotScopeId)) {
			return true;
		}

		// Hierarchy match: attribute scope is descendant of slot scope
		return tagResolver.getDescendants(slotScopeId).contains(attrScope);
	}

	/**
	 * Returns all rules for inspection/testing.
	 *
	 * @return Immutable list of all rules in evaluation order
	 */
	public static List<ScopeMatchRule> getRules() {
		return RULES;
	}

	/**
	 * Finds which rule matched (for debugging/logging).
	 *
	 * @param attributeScope The attribute's scope
	 * @param slotScope The slot's scope
	 * @return The first matching rule, or empty if fallback was used
	 */
	public static Optional<ScopeMatchRule> findMatchingRule(
			Optional<OpenIdentifier> attributeScope,
			Optional<OpenIdentifier> slotScope
	) {
		return RULES.stream()
				.filter(rule -> rule.applies(attributeScope, slotScope))
				.findFirst();
	}

	/**
	 * Explains why a match result was produced (for debugging).
	 *
	 * @param attributeScope The attribute's scope
	 * @param slotScope The slot's scope
	 * @return Human-readable explanation of the match result
	 */
	public static String explain(Optional<OpenIdentifier> attributeScope, Optional<OpenIdentifier> slotScope) {
		Optional<ScopeMatchRule> matchedRule = findMatchingRule(attributeScope, slotScope);
		boolean result = matches(attributeScope, slotScope);

		if (matchedRule.isPresent()) {
			return String.format("Result: %s - Rule: %s",
					result, matchedRule.get().description());
		} else {
			return String.format("Result: %s - Fallback: exact match comparison (attr=%s, slot=%s)",
					result, attributeScope.orElse(null), slotScope.orElse(null));
		}
	}
}
