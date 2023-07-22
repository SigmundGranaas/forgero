package com.sigmundgranaas.forgero.core.model;

import java.util.Optional;

import com.sigmundgranaas.forgero.core.model.v1.match.StatefulMatcher;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;
import org.jetbrains.annotations.NotNull;

public record ModelMatchPairing(Matchable match, ModelMatcher model) implements ModelMatcher {

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
		if (o instanceof StatefulMatcher comparer && match instanceof StatefulMatcher matcher) {
			return comparer.getPredicates().size() - matcher.getPredicates().size();
		}
		return 0;
	}
}
