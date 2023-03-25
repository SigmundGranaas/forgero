package com.sigmundgranaas.forgero.core.model;

import com.sigmundgranaas.forgero.core.util.match.Context;
import com.sigmundgranaas.forgero.core.util.match.Matchable;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface ModelMatcher extends Comparable<ModelMatcher> {
	ModelMatcher EMPTY = new ModelMatcher() {
		@Override
		public int compareTo(@NotNull ModelMatcher o) {
			return comparator(this, o);
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

			//If both reference an identifier, the one with the most criteria is preferred
			if(match1Identifier && match2Identifier){
				return Integer.compare(entry1.match().criteria().size(), entry2.match().criteria().size());
			}

			//If one references an identifier, it is preferred
			if (match1Identifier) {
				return -1;
			} else if (match2Identifier) {
				return 1;
			}

			//If neither references an identifier, the one with the most criteria is preferred
			return Integer.compare(entry1.match().criteria().size(), entry2.match().criteria().size());
		}
		return 0;
	}

	boolean match(Matchable state, Context context);

	Optional<ModelTemplate> get(Matchable state, ModelProvider provider, Context context);

	@Override
	default int compareTo(@NotNull ModelMatcher o) {
		return comparator(this, o);
	}
}
