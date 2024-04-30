package com.sigmundgranaas.forgero.core.api.identity.sorting;

import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.sigmundgranaas.forgero.core.state.State;

/**
 * A registry to keep track of the current sorting rules.
 * Each rules can be overridden. The local registry can be used for testing, while the static registry is used by default when running the client.
 */
public class SortingRuleRegistry {
	private static final Map<String, SortingRule> staticRules = new ConcurrentHashMap<>();
	private final Map<String, SortingRule> rules;

	private SortingRuleRegistry() {
		this.rules = staticRules;
	}

	private SortingRuleRegistry(Map<String, SortingRule> rules) {
		this.rules = new ConcurrentHashMap<>(rules);
	}

	public static SortingRuleRegistry staticRegistry() {
		return new SortingRuleRegistry();
	}

	public static SortingRuleRegistry localRegistry(Map<String, SortingRule> rules) {
		return new SortingRuleRegistry(rules);
	}

	public static SortingRuleRegistry local() {
		return new SortingRuleRegistry(new ConcurrentHashMap<>());
	}

	public void registerRule(String id, SortingRule rule) {
		rules.put(id, rule);
	}

	public Comparator<State> getComparator() {
		return (s1, s2) -> {
			Integer p1 = rules.values().stream()
					.filter(rule -> rule.applies(s1))
					.map(SortingRule::getPriority)
					.findFirst()
					.orElse(Integer.MAX_VALUE);

			Integer p2 = rules.values().stream()
					.filter(rule -> rule.applies(s2))
					.map(SortingRule::getPriority)
					.findFirst()
					.orElse(Integer.MAX_VALUE);

			return Integer.compare(p1, p2);
		};
	}
}

