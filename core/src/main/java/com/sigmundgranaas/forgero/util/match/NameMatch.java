package com.sigmundgranaas.forgero.util.match;

import com.sigmundgranaas.forgero.state.Identifiable;

public record NameMatch(String name) implements Matchable {
    @Override
    public boolean test(Matchable match, Context context) {
        if (match instanceof Identifiable id) {
            return id.name().equals(name);
        } else {
            return context.test(this, context);
        }
    }
}
