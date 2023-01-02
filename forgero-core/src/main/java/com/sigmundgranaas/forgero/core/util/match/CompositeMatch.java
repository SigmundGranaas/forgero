package com.sigmundgranaas.forgero.core.util.match;

import com.sigmundgranaas.forgero.core.state.Composite;

public class CompositeMatch implements Matchable {
    @Override
    public boolean test(Matchable match, Context context) {
        return match instanceof Composite;
    }
}
