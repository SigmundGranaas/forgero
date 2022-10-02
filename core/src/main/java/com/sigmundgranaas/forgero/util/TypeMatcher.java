package com.sigmundgranaas.forgero.util;

import com.sigmundgranaas.forgero.util.match.MatchContext;
import com.sigmundgranaas.forgero.util.match.Matchable;

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
