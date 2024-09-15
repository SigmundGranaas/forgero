package com.sigmundgranaas.forgero.core.model;

import java.util.Optional;

import com.sigmundgranaas.forgero.core.model.match.PredicateMatcher;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;
import org.jetbrains.annotations.NotNull;

public record ModelMatchPairing(PredicateMatcher match, ModelMatcher model) implements ModelMatcher {

	@Override
	public boolean match(Matchable state, MatchContext context) {
		return match.test(state, context);
	}

	@Override
	public Optional<ModelTemplate> get(Matchable matchable, ModelProvider provider, MatchContext context) {
		if (match.test(matchable, context)) {
			return model.get(matchable, provider, context);
		}
		return Optional.empty();
	}

	@Override
	public int compareTo(@NotNull ModelMatcher o) {
		if (o instanceof PredicateMatcher comparer) {
			return comparer.getPredicates().size() - match.getPredicates().size();
		}
		return ModelMatcher.comparator(this, o);
	}
}