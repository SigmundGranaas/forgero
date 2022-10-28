package com.sigmundgranaas.forgero.state;

import com.sigmundgranaas.forgero.property.Property;
import com.sigmundgranaas.forgero.property.PropertyContainer;
import com.sigmundgranaas.forgero.type.Type;
import com.sigmundgranaas.forgero.util.match.Context;
import com.sigmundgranaas.forgero.util.match.Matchable;
import com.sigmundgranaas.forgero.util.match.NameMatch;

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

    default int getEntityCount() {
        return 1;
    }

    Type type();

    default boolean equals(State s) {
        return false;
    }

    @Override
    default boolean test(Matchable match, Context context) {
        if (match instanceof Type typeMatch) {
            if (this.type().test(typeMatch, context)) {
                return true;
            }
        }
        if (match instanceof NameMatch name) {
            return name.test(this, context);
        }
        return false;
    }
}
