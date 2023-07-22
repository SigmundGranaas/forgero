package com.sigmundgranaas.forgero.core.model.v1.match;

import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.JsonElement;
import com.sigmundgranaas.forgero.core.util.match.Matchable;

/**
 * Abstract class responsible for holding the common behavior and attributes for Matchers.
 */
public abstract class StatefulMatcher implements Matchable {
	protected final List<Matchable> predicates;

	/**
	 * @param predicates       The list of predicates to match against.
	 * @param predicateFactory The factory to create predicates.
	 */
	public StatefulMatcher(List<JsonElement> predicates, PredicateFactory predicateFactory) {
		this.predicates = predicates.stream()
				.map(predicateFactory::create)
				.collect(Collectors.toList());
	}

	public static StatefulMatcher of(List<JsonElement> predicates, PredicateFactory factory) {
		return new DefaultMatcher(predicates, factory);
	}

	public List<Matchable> getPredicates() {
		return predicates;
	}

}
