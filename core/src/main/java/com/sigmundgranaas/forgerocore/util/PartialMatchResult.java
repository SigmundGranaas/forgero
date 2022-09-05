package com.sigmundgranaas.forgerocore.util;

public class PartialMatchResult implements MatchResult {
    @Override
    public boolean is() {
        return true;
    }

    @Override
    public boolean is(MatchContext context) {
        if (context == MatchContext.INGREDIENT) {
            return false;
        }
        return true;
    }
}
