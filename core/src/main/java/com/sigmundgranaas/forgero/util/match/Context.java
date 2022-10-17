package com.sigmundgranaas.forgero.util.match;

import java.util.ArrayList;
import java.util.List;

public class Context implements Matchable {
    private final List<Matchable> matches;

    public static Context of() {
        return new Context();
    }

    public static Context of(List<Matchable> matchable) {
        return new Context(matchable);
    }

    public Context() {
        this.matches = new ArrayList<>();
    }

    public Context(List<Matchable> matchable) {
        this.matches = new ArrayList<>(matchable);
    }

    public Context add(Matchable matchable) {
        if (!matches.contains(matchable)) {
            this.matches.add(matchable);
        }
        return this;
    }

    @Override
    public boolean test(Matchable match, Context context) {
        return matches.stream().anyMatch(matchable -> matchable.test(match, context));
    }
}
