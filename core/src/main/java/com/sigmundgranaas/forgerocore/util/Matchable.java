package com.sigmundgranaas.forgerocore.util;

public interface Matchable {
    boolean test(Matchable match);

    boolean test(Matchable match, MatchContext context);
}
