package com.sigmundgranaas.forgerocore.util;

public class SchematicMatcher extends TypeMatcher {
    @Override
    public boolean test(Matchable match, MatchContext context) {
        return context != MatchContext.COMPOSITE;
    }
}
