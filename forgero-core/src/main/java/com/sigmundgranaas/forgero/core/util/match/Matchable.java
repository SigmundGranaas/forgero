package com.sigmundgranaas.forgero.core.util.match;

@FunctionalInterface
public interface Matchable {
	Matchable DEFAULT_FALSE = (match, context) -> false;
	Matchable DEFAULT_TRUE = (match, context) -> false;


	boolean test(Matchable match, MatchContext context);

	default boolean test(Matchable matchable) {
		return test(matchable, MatchContext.of());
	}
}
