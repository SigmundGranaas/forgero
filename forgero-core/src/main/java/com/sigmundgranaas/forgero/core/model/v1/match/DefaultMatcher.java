package com.sigmundgranaas.forgero.core.model.v1.match;

import java.util.List;

import com.google.gson.JsonElement;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;

/**
 * This class extends the abstract class StatefulMatcher, it holds the logic for
 * testing a Matchable object against a given set of criteria.
 */
public class DefaultMatcher extends PredicateMatcher {

	/**
	 * Constructor for the DefaultMatcher class
	 *
	 * @param criteria         The list of criteria in the form of JsonElements to match against.
	 * @param predicateFactory The factory to create predicates for matching.
	 */
	public DefaultMatcher(List<JsonElement> criteria, PredicateFactory predicateFactory) {
		super(criteria, predicateFactory);
	}

	/**
	 * Tests the Matchable object against the specified set of criteria.
	 * It checks whether the Matchable object matches with CompositeStrategy,
	 * or all predicates, or SlotStrategy.
	 *
	 * @param match   The Matchable object to test against the criteria.
	 * @param context The context in which the Matchable object is tested.
	 * @return Boolean value representing whether the Matchable object matches any of the criteria.
	 */
	@Override
	public boolean test(Matchable match, MatchContext context) {
		return new CompositeStrategy(getPredicates()).test(match, context)
				|| predicates.stream().allMatch(pred -> pred.test(match, context))
				|| new SlotStrategy(getPredicates()).test(match, context);
	}
}
