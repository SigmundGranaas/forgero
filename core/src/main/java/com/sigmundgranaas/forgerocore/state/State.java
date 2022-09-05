package com.sigmundgranaas.forgerocore.state;

import com.sigmundgranaas.forgerocore.property.PropertyContainer;
import com.sigmundgranaas.forgerocore.type.Type;
import com.sigmundgranaas.forgerocore.util.Matchable;

public interface State extends PropertyContainer, Matchable, Identifiable {
    Type type();

    default boolean equals(State s) {
        return false;
    }
}
