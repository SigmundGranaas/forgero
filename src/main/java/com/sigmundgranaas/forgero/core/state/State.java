package com.sigmundgranaas.forgero.core.state;

import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.type.Type;
import com.sigmundgranaas.forgero.core.util.Matchable;

public interface State extends PropertyContainer, Matchable, Identifiable {
    Type type();

    default boolean equals(State s) {
        return false;
    }
}
