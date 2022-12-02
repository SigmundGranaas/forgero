package com.sigmundgranaas.forgero.util;

import com.sigmundgranaas.forgero.util.match.Context;
import com.sigmundgranaas.forgero.util.match.Matchable;

public class TypeMatcher implements Matchable {

    @Override
    public boolean test(Matchable match, Context context) {
        return true;
    }
}
