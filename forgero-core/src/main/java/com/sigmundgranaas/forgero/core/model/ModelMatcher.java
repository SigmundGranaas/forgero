package com.sigmundgranaas.forgero.core.model;

import java.util.Optional;

import com.sigmundgranaas.forgero.core.model.v1.match.PredicateMatcher;
import com.sigmundgranaas.forgero.core.model.v1.match.predicate.IdPredicate;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;
import org.jetbrains.annotations.NotNull;

public interface ModelMatcher extends Comparable<ModelMatcher> {
	ModelMatcher EMPTY = new ModelMatcher() {
		@Override
		public int compareTo(@NotNull ModelMatcher o) {
			return comparator(this, o);
		}

		@Override
		public boolean match(Matchable state, MatchContext context) {
			return false;
		}

		@Override
		public Optional<ModelTemplate> get(Matchable state, ModelProvider provider, MatchContext context) {
			return Optional.empty();
		}
	};

	static int comparator(ModelMatcher match1, ModelMatcher match2) {
		if (match1 instanceof ModelMatchPairing entry1 && match2 instanceof ModelMatchPairing entry2) {
			if (entry1.match() instanceof PredicateMatcher matcher1 && entry2.match() instanceof PredicateMatcher matcher2) {
				var match1Identifier = matcher1.getPredicates().stream().anyMatch(match -> match instanceof IdPredicate);
				var match2Identifier = matcher2.getPredicates().stream().anyMatch(match -> match instanceof IdPredicate);

				//If both reference an identifier, the one with the most criteria is preferred
				if (match1Identifier && match2Identifier) {
					return Integer.compare(matcher1.getPredicates().size(), matcher2.getPredicates().size());
				}

				//If one references an identifier, it is preferred
				if (match1Identifier) {
					return -1;
				} else if (match2Identifier) {
					return 1;
				}

				//If neither references an identifier, the one with the most criteria is preferred
				return Integer.compare(matcher1.getPredicates().size(), matcher2.getPredicates().size());
			}

		}
		return 0;
	}

	boolean match(Matchable state, MatchContext context);

	Optional<ModelTemplate> get(Matchable state, ModelProvider provider, MatchContext context);

	@Override
	default int compareTo(@NotNull ModelMatcher o) {
		return comparator(this, o);
	}
}
