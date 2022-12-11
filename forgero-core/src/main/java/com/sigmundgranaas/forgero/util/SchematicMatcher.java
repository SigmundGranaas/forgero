package com.sigmundgranaas.forgero.util;

import com.sigmundgranaas.forgero.state.Composite;
import com.sigmundgranaas.forgero.state.State;
import com.sigmundgranaas.forgero.type.Type;
import com.sigmundgranaas.forgero.util.match.Context;
import com.sigmundgranaas.forgero.util.match.Matchable;

public class SchematicMatcher extends TypeMatcher {
    @Override
    public boolean test(Matchable match, Context context) {
        if (match instanceof Composite) {
            return false;
        } else if (match instanceof State state) {
            return state.type().test(Type.of("SCHEMATIC"), context);
        } else if (match instanceof Type type) {
            if (type.typeName().equals("SCHEMATIC")) {
                return true;
            } else {
                return type.parent().map(parent -> test(parent, context)).orElse(false);
            }
        }
        return false;
    }
}
