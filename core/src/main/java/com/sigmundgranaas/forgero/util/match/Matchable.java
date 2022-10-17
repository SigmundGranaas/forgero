package com.sigmundgranaas.forgero.util.match;

public interface Matchable {
    boolean test(Matchable match, Context context);
}
