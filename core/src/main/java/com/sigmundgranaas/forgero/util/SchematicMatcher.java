package com.sigmundgranaas.forgero.util;

import com.sigmundgranaas.forgero.util.match.MatchContext;
import com.sigmundgranaas.forgero.util.match.Matchable;

public class SchematicMatcher extends TypeMatcher {
    @Override
    public boolean test(Matchable match, MatchContext context) {
        return context != MatchContext.COMPOSITE;
    }
}
