package com.sigmundgranaas.forgero.core.model;

import com.sigmundgranaas.forgero.core.util.match.Context;
import com.sigmundgranaas.forgero.core.util.match.Matchable;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface ModelMatcher extends Comparable<ModelMatcher> {
	ModelMatcher EMPTY = new ModelMatcher() {
		@Override
		public int compareTo(@NotNull ModelMatcher o) {
			return 0;
		}

		@Override
		public boolean match(Matchable state, Context context) {
			return false;
		}

		@Override
		public Optional<ModelTemplate> get(Matchable state, ModelProvider provider, Context context) {
			return Optional.empty();
		}
	};

	static int comparator(ModelMatcher match1, ModelMatcher matcher2) {
		if (match1 instanceof ModelMatchPairing entry1 && matcher2 instanceof ModelMatchPairing entry2) {
			var match1Identifier = entry1.match().criteria().stream().anyMatch(match -> match.contains("id:"));
			var match2Identifier = entry2.match().criteria().stream().anyMatch(match -> match.contains("id:"));

			if (match1Identifier) {
				if (match2Identifier) {
					return 0;
				} else {
					return -1;
				}
			} else {
				if (match2Identifier) {
					return 1;
				}
				return 0;
			}
		}
		return 0;
	}

	boolean match(Matchable state, Context context);

	Optional<ModelTemplate> get(Matchable state, ModelProvider provider, Context context);

	@Override
	default int compareTo(@NotNull ModelMatcher o) {
		return 0;
	}
}
