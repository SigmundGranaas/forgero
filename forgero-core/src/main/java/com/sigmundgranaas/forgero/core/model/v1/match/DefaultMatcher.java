package com.sigmundgranaas.forgero.core.model.v1.match;

import java.util.List;

import com.google.gson.JsonElement;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;

public class DefaultMatcher extends StatefulMatcher {

	/**
	 * @param criteria         The list of criteria to match against.
	 * @param predicateFactory The factory to create predicates.
	 */
	public DefaultMatcher(List<JsonElement> criteria, PredicateFactory predicateFactory) {
		super(criteria, predicateFactory);
	}

	@Override
	public boolean test(Matchable match, MatchContext context) {
		return false;
	}
}
