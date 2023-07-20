package com.sigmundgranaas.forgero.core.model.v1.match;

import java.util.List;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;

public class DefaultMatcher extends Matcher {

	/**
	 * @param criteria         The list of criteria to match against.
	 * @param predicateFactory The factory to create predicates.
	 */
	public DefaultMatcher(List<JsonObject> criteria, ModelPredicateFactory predicateFactory) {
		super(criteria, predicateFactory);
	}

	@Override
	public boolean test(Matchable match, MatchContext context) {
		return false;
	}
}
