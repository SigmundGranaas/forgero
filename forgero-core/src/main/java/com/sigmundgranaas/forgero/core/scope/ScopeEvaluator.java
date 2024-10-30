package com.sigmundgranaas.forgero.core.scope;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

public class ScopeEvaluator {
	private final Set<ScopedTest> rules;
	private final Set<ScopedTest> disqualifiers;

	private ScopeEvaluator(Set<ScopedTest> rules, Set<ScopedTest> disqualifiers) {
		this.rules = Set.copyOf(rules);
		this.disqualifiers = Set.copyOf(disqualifiers);
	}

	public static ScopeEvaluator of(Set<ScopedTest> rules, Set<ScopedTest> disqualifiers) {
		return new ScopeEvaluator(rules, disqualifiers);
	}

	public EvaluationResult evaluate(Scope scope) {
		// Check disqualifiers first - fast fail
		for (ScopedTest disqualifier : disqualifiers) {
			if (disqualifier.isCompatible(scope)) {
				return new EvaluationResult(ValidationStatus.INVALID, this);
			}
		}

		// Evaluate all applicable rules
		Set<ScopedTest> matched = new HashSet<>();
		Set<ScopedTest> unmatched = new HashSet<>();

		for (ScopedTest rule : rules) {
			if (rule.test(scope)) {
				matched.add(rule);
			} else {
				unmatched.add(rule);
			}
		}

		if (matched.isEmpty() && unmatched.isEmpty()) {
			return new EvaluationResult(ValidationStatus.INVALID, this);
		}

		if (unmatched.isEmpty()) {
			return new EvaluationResult(ValidationStatus.VALID, this);
		}

		// Create new evaluator without matched rules
		ScopeEvaluator partialEvaluator = new ScopeEvaluator(unmatched, disqualifiers);
		return new EvaluationResult(ValidationStatus.PARTIAL, partialEvaluator);
	}

	public boolean matches(Scope scope) {
		return Stream.concat(rules.stream(), disqualifiers.stream()).allMatch(rule -> rule.isCompatible(scope));
	}

	public enum ValidationStatus {
		VALID,
		PARTIAL,
		INVALID
	}

	public record EvaluationResult(ValidationStatus status, ScopeEvaluator remainingEvaluator) {}
}
