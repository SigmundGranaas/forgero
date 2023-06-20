package com.sigmundgranaas.forgero.core.util.match;

public interface Matchable {
	boolean test(Matchable match, MatchContext context);

	default boolean test(Matchable matchable) {
		return test(matchable, MatchContext.of());
	}
}
