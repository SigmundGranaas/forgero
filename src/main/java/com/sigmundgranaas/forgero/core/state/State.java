package com.sigmundgranaas.forgero.core.state;

import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.util.Type;

public interface State<T> {
    String name();

    PropertyContainer properties();

    Type type();

    default boolean equals(State<?> s) {
        return false;
    }
}
