package com.sigmundgranaas.forgero.core.api.v0.identity;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import com.sigmundgranaas.forgero.core.state.State;

/**
 * A registry to keep track of the current modification rules.
 * Each rules can be overridden. The local registry can be used for testing, while the static registry is used by default when running the client.
 */
public class ModificationRuleRegistry {
	private static final Map<String, ModificationRule> staticRules = new ConcurrentHashMap<>();
	private final Map<String, ModificationRule> rules;

	private ModificationRuleRegistry() {
		this.rules = staticRules;
	}

	private ModificationRuleRegistry(Map<String, ModificationRule> rules) {
		this.rules = rules;
	}

	public static ModificationRuleRegistry staticRegistry() {
		return new ModificationRuleRegistry();
	}

	public static ModificationRuleRegistry localRegistry(Map<String, ModificationRule> rules) {
		return new ModificationRuleRegistry(rules);
	}

	public static ModificationRuleRegistry local() {
		return new ModificationRuleRegistry(new ConcurrentHashMap<>());
	}

	public void registerRule(String id, ModificationRule rule) {
		rules.put(id, rule);
	}

	public String applyRules(State state) {
		String originalName = state.name();
		return rules.values().stream()
				.filter(rule -> rule.applies(state))
				.map(ModificationRule::transformation)
				.reduce(Function.identity(), Function::andThen)
				.apply(originalName);
	}
}
