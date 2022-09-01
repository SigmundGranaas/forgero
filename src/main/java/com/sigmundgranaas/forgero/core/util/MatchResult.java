package com.sigmundgranaas.forgero.core.util;

@FunctionalInterface
public interface MatchResult {
    MatchResult PARTIAL = new PartialMatchResult();
    MatchResult EXACT = new ExactResult();

    default boolean is() {
        return true;
    }

    boolean is(MatchContext context);
}
