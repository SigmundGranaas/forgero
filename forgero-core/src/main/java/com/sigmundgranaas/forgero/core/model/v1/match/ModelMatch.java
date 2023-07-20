package com.sigmundgranaas.forgero.core.model.v1.match;

import java.util.List;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;

/**
 * This class is responsible for matching Forgero states based on the given criteria and matchType.
 */
public class ModelMatch implements Matchable {
	private final Matcher matcher;

	/**
	 * @param predicates       The list of predicates to match against.
	 * @param matchType        The type of match to be performed.
	 * @param predicateFactory The factory to create predicates.
	 */
	public ModelMatch(List<JsonObject> predicates, String matchType, ModelPredicateFactory predicateFactory) {
		// Based on the matchType, select the appropriate Matcher.
		switch (matchType) {
			case "UPGRADE" -> this.matcher = new UpgradeMatcher(predicates, predicateFactory);
			case "COMPOSITE" -> this.matcher = new CompositeMatcher(predicates, predicateFactory);
			default -> this.matcher = new DefaultMatcher(predicates, predicateFactory);
		}
	}

	/**
	 * @param context The MatchContext to test against.
	 * @return Whether the match passes.
	 */
	public boolean test(Matchable match, MatchContext context) {
		return matcher.test(match, context);
	}
}
