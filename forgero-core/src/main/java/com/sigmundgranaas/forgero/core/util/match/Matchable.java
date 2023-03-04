package com.sigmundgranaas.forgero.core.util.match;

public interface Matchable {
	boolean test(Matchable match, Context context);

	default boolean test(Matchable matchable) {
		return test(matchable, Context.of());
	}
}
