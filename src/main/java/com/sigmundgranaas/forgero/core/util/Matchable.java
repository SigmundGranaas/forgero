package com.sigmundgranaas.forgero.core.util;

public interface Matchable {
    boolean test(Matchable match);

    boolean test(Matchable match, MatchContext context);
}
