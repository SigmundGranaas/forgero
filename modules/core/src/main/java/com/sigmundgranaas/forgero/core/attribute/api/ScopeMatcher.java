package com.sigmundgranaas.forgero.core.attribute.api;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;

import java.util.Optional;
import java.util.Set;

/**
 * Matcher for scope values in decision table rules.
 *
 * <p>This sealed interface provides pattern matching for attribute and slot scopes
 * in the {@link ScopeMatchDecisionTable}. Each implementation defines a specific
 * matching strategy.</p>
 */
public sealed interface ScopeMatcher {

	/**
	 * Tests if this matcher matches the given scope.
	 *
	 * @param scope The scope to test (may be empty for default/no-scope attributes)
	 * @return true if this matcher matches the scope
	 */
	boolean matches(Optional<OpenIdentifier> scope);

	/**
	 * Matches if scope is empty (no scope defined).
	 *
	 * <p>Used for matching default attributes that have no explicit scope.</p>
	 */
	record Empty() implements ScopeMatcher {
		@Override
		public boolean matches(Optional<OpenIdentifier> scope) {
			return scope.isEmpty();
		}

		@Override
		public String toString() {
			return "Empty";
		}
	}

	/**
	 * Matches a specific scope identifier exactly.
	 *
	 * @param expected The expected scope identifier
	 */
	record Exact(OpenIdentifier expected) implements ScopeMatcher {
		@Override
		public boolean matches(Optional<OpenIdentifier> scope) {
			return scope.map(s -> s.equals(expected)).orElse(false);
		}

		@Override
		public String toString() {
			return "Exact(" + expected + ")";
		}
	}

	/**
	 * Matches any of the given scope identifiers.
	 *
	 * @param scopes The set of scope identifiers to match against
	 */
	record AnyOf(Set<OpenIdentifier> scopes) implements ScopeMatcher {
		public AnyOf {
			scopes = Set.copyOf(scopes); // Defensive copy
		}

		@Override
		public boolean matches(Optional<OpenIdentifier> scope) {
			return scope.map(scopes::contains).orElse(false);
		}

		@Override
		public String toString() {
			return "AnyOf(" + scopes + ")";
		}
	}

	/**
	 * Matches any scope (including empty).
	 *
	 * <p>This is a wildcard matcher used when the rule should apply regardless
	 * of the scope value.</p>
	 */
	record Any() implements ScopeMatcher {
		@Override
		public boolean matches(Optional<OpenIdentifier> scope) {
			return true;
		}

		@Override
		public String toString() {
			return "Any";
		}
	}

	/**
	 * Matches non-empty scopes that are NOT in the exclusion set.
	 *
	 * <p>Used to match "custom" scopes - scopes that are not built-in
	 * (like PART_COMPOSITE, EQUIPMENT_COMPOSITE, UPGRADE, LOCAL).</p>
	 *
	 * @param excluded The set of scopes to exclude from matching
	 */
	record CustomScope(Set<OpenIdentifier> excluded) implements ScopeMatcher {
		public CustomScope {
			excluded = Set.copyOf(excluded); // Defensive copy
		}

		@Override
		public boolean matches(Optional<OpenIdentifier> scope) {
			return scope.map(s -> !excluded.contains(s)).orElse(false);
		}

		@Override
		public String toString() {
			return "CustomScope(excluding " + excluded + ")";
		}
	}

	/**
	 * Matches non-empty scopes (any scope that is present).
	 *
	 * <p>Used to match any attribute that has an explicit scope defined.</p>
	 */
	record NonEmpty() implements ScopeMatcher {
		@Override
		public boolean matches(Optional<OpenIdentifier> scope) {
			return scope.isPresent();
		}

		@Override
		public String toString() {
			return "NonEmpty";
		}
	}
}
