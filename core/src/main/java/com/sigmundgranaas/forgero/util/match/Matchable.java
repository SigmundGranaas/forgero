package com.sigmundgranaas.forgero.util.match;

public interface Matchable {
    boolean test(Matchable match);

    boolean test(Matchable match, MatchContext context);
}
