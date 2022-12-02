package com.sigmundgranaas.forgero.util.match;

public class Fail implements MatchResult {
    @Override
    public boolean match() {
        return false;
    }

    @Override
    public boolean fail() {
        return true;
    }

    @Override
    public int matchGrade() {
        return 0;
    }
}
