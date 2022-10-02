package com.sigmundgranaas.forgero.util.match;

public interface MatchResult {
    boolean match();

    boolean fail();

    public int matchGrade();
}
