package com.sigmundgranaas.forgero.core.state;

import com.sigmundgranaas.forgero.core.property.Property;
import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.util.Matchable;
import com.sigmundgranaas.forgero.core.util.Type;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface Ingredient extends PropertyContainer, Matchable, Identifiable {
    static Ingredient of(Composite composite) {
        return new CompositeIngredient(composite);
    }

    static Ingredient of(String name, Type type, List<Property> properties) {
        return new SimpleIngredient(name, type, properties);
    }

    default int quantity() {
        return 1;
    }

    @NotNull
    Type type();
}
