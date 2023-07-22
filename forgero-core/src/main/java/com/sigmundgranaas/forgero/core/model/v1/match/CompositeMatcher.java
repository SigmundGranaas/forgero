package com.sigmundgranaas.forgero.core.model.v1.match;

import java.util.List;
import java.util.Optional;

import com.google.gson.JsonElement;
import com.sigmundgranaas.forgero.core.state.Composite;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.state.composite.Constructed;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;

public class CompositeMatcher extends StatefulMatcher {
	public CompositeMatcher(List<JsonElement> predicates, PredicateFactory predicateFactory) {
		super(predicates, predicateFactory);
	}

	@Override
	public boolean test(Matchable match, MatchContext context) {
		Optional<State> stateOpt = context.get("state", State.class);
		if (stateOpt.isPresent() && stateOpt.get() instanceof Composite composite) {
			if (composite instanceof Constructed constructed) {
				List<Matchable> matchedPredicates = predicates.stream()
						.filter(predicate -> predicate.test(match, context))
						.toList();
				return matchedPredicates.size() == predicates.size();
			}
			return predicates.stream().anyMatch(predicate -> predicate.test(context));
		}
		return false;
	}
}
