package com.sigmundgranaas.forgero.core.attribute.api;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;

import java.util.Optional;

/**
 * A single rule in the scope matching decision table.
 *
 * <p>Rules are evaluated in order - the first matching rule determines the result.
 * Each rule consists of:</p>
 * <ul>
 *   <li>{@code attributeMatcher} - Matches the attribute's scope</li>
 *   <li>{@code slotMatcher} - Matches the slot's scope</li>
 *   <li>{@code result} - The result if both matchers match</li>
 *   <li>{@code description} - Human-readable explanation of the rule</li>
 * </ul>
 *
 * @param attributeMatcher Matcher for the attribute's scope
 * @param slotMatcher Matcher for the slot's scope
 * @param result The result to return if this rule matches
 * @param description Human-readable description of what this rule does
 */
public record ScopeMatchRule(
		ScopeMatcher attributeMatcher,
		ScopeMatcher slotMatcher,
		boolean result,
		String description
) {

	/**
	 * Checks if this rule applies to the given scopes.
	 *
	 * @param attrScope The attribute's scope (may be empty)
	 * @param slotScope The slot's scope (may be empty)
	 * @return true if both matchers match their respective scopes
	 */
	public boolean applies(Optional<OpenIdentifier> attrScope, Optional<OpenIdentifier> slotScope) {
		return attributeMatcher.matches(attrScope) && slotMatcher.matches(slotScope);
	}

	@Override
	public String toString() {
		return String.format("ScopeMatchRule[attr=%s, slot=%s -> %s: %s]",
				attributeMatcher, slotMatcher, result, description);
	}
}
