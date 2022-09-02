package com.sigmundgranaas.forgero.core.state;

import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.util.Matchable;
import com.sigmundgranaas.forgero.core.util.Type;

public interface State extends PropertyContainer, Matchable {
    String name();

    String nameSpace();

    default String identifier() {
        return String.format("%s#%s", nameSpace(), name());
    }

    Type type();

    default boolean equals(State s) {
        return false;
    }
}
