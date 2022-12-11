package com.sigmundgranaas.forgero.util.match;

import com.sigmundgranaas.forgero.state.Composite;

public class CompositeMatch implements Matchable {
    @Override
    public boolean test(Matchable match, Context context) {
        return match instanceof Composite;
    }
}
