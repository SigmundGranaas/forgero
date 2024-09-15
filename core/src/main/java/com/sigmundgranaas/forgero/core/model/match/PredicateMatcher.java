package com.sigmundgranaas.forgero.core.model.match;

import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.JsonElement;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;

/**
 * This abstract class is responsible for holding the common behavior and attributes for Matchers.
 * It contains a list of predicates that are used in matching operations.
 */
public abstract class PredicateMatcher implements Matchable {

	/**
	 * A list of predicates used for matching against Matchable objects.
	 */
	protected final List<Matchable> predicates;

	/**
	 * Constructor for the StatefulMatcher abstract class.
	 *
	 * @param predicates       The list of predicates in the form of JsonElements to match against.
	 * @param predicateFactory The factory to create predicates for matching.
	 */
	public PredicateMatcher(List<JsonElement> predicates, PredicateFactory predicateFactory) {
		this.predicates = predicates.stream()
				.map(predicateFactory::create)
				.collect(Collectors.toList());
	}

	/**
	 * Factory method for the creation of a DefaultMatcher object.
	 *
	 * @param predicates The list of predicates in the form of JsonElements to match against.
	 * @param factory    The factory to create predicates for matching.
	 * @return A DefaultMatcher object created with the given predicates and factory.
	 */
	public static PredicateMatcher of(List<JsonElement> predicates, PredicateFactory factory) {
		return new DefaultMatcher(predicates, factory);
	}

	/**
	 * Getter for the list of predicates.
	 *
	 * @return The list of predicates.
	 */
	public List<Matchable> getPredicates() {
		return predicates;
	}

	/**
	 * Getter for the list of dynamic predicates.
	 *
	 * @return The list of predicates.
	 */
	public List<Matchable> getDynamicPredicates() {
		return getPredicates().stream().filter(Matchable::isDynamic).toList();
	}

	public abstract boolean testDynamic(Matchable match, MatchContext context);
}
