package com.sigmundgranaas.forgero.state;

import com.sigmundgranaas.forgero.property.Property;
import com.sigmundgranaas.forgero.property.PropertyContainer;
import com.sigmundgranaas.forgero.type.Type;
import com.sigmundgranaas.forgero.util.Matchable;

import java.util.List;

public interface State extends PropertyContainer, Matchable, Identifiable {
    static State of(Composite composite) {
        return new CompositeIngredient(composite);
    }

    static State of(String name, Type type, List<Property> properties) {
        return new SimpleState(name, type, properties);
    }

    static Ingredient of(String name, String nameSpace, Type type, List<Property> properties) {
        return new SimpleState(name, nameSpace, type, properties);
    }

    Type type();

    default boolean equals(State s) {
        return false;
    }
}
