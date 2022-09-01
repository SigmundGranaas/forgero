package com.sigmundgranaas.forgero.core.util;

import java.util.Optional;

public record SimpleType(String name, Optional<Type> parent, TypeMatcher matcher) implements Type {
    @Override
    public boolean test(Matchable match) {
        return test(match, MatchContext.DEFAULT);
    }

    @Override
    public boolean test(Matchable match, MatchContext context) {
        if (match instanceof Type type) {
            if (name.equals(type.typeName())) {
                return matcher.test(match, context);
            } else if (parent.isPresent()) {
                return parent.get().test(match, context);
            }
        }
        return false;
    }

    @Override
    public String typeName() {
        return name;
    }
}
