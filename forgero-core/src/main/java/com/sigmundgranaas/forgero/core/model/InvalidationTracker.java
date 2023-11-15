package com.sigmundgranaas.forgero.core.model;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.function.Function;

import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;

/**
 * Represents a set of checks to detect potential invalidations.
 * It keeps track of the initial set of check results,
 * and provides functionality to compare new results with the initial ones.
 */
public class InvalidationTracker implements Matchable {
	/**
	 * Holds the initial encoded results of checks.
	 */
	private final BitSet initialCheckResults = new BitSet();

	/**
	 * List of ordered checks to perform.
	 */
	public List<Function<MatchContext, Boolean>> checks = new ArrayList<>();

	/**
	 * Adds a single check to the list and records its initial result.
	 *
	 * @param check   The check function to be added.
	 * @param context Context to provide to the check function.
	 * @return The current instance of {@link InvalidationTracker} for chaining.
	 */
	public InvalidationTracker addCheck(Function<MatchContext, Boolean> check, MatchContext context) {
		checks.add(check);
		if (check.apply(context)) {
			initialCheckResults.set(checks.size() - 1);
		}
		return this;
	}

	/**
	 * Adds a list of checks and records their initial results.
	 *
	 * @param checksList List of check functions to be added.
	 * @param context    Context to provide to each check function.
	 * @return The current instance of {@link InvalidationTracker} for chaining.
	 */
	public InvalidationTracker addAllChecks(List<Function<MatchContext, Boolean>> checksList, MatchContext context) {
		for (Function<MatchContext, Boolean> check : checksList) {
			addCheck(check, context);
		}
		return this;
	}

	/**
	 * Tests if the results from the current context match the initial results.
	 *
	 * @param match   Not used in the current implementation.
	 * @param context Current context to provide to each check function.
	 * @return True if there's a change in results, indicating a potential invalidation.
	 */
	@Override
	public boolean test(Matchable match, MatchContext context) {
		BitSet currentCheckResults = new BitSet();
		for (int i = 0; i < checks.size(); i++) {
			if (checks.get(i).apply(context)) {
				currentCheckResults.set(i);
			}
		}
		return !initialCheckResults.equals(currentCheckResults);
	}
}
