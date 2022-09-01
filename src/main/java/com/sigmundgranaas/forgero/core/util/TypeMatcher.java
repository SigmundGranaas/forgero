package com.sigmundgranaas.forgero.core.util;

public class TypeMatcher implements Matchable {

    @Override
    public boolean test(Matchable match) {
        return true;
    }

    @Override
    public boolean test(Matchable match, MatchContext context) {
        return true;
    }
}
