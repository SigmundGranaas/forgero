package com.sigmundgranaas.forgero.util.match;

import com.sigmundgranaas.forgero.state.Identifiable;

public record NameMatch(String name) implements Matchable {
    @Override
    public boolean test(Matchable match) {
        if (match instanceof Identifiable id) {
            return id.name().equals(name);
        } else {
            return false;
        }
    }

    @Override
    public boolean test(Matchable match, MatchContext context) {
        return test(match);
    }
}
