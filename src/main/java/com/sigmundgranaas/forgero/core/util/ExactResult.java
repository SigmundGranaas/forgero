package com.sigmundgranaas.forgero.core.util;

public class ExactResult implements MatchResult {
    @Override
    public boolean is() {
        return true;
    }

    @Override
    public boolean is(MatchContext context) {
        return false;
    }
}
