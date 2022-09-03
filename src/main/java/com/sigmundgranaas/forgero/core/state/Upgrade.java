package com.sigmundgranaas.forgero.core.state;

import com.sigmundgranaas.forgero.core.property.Property;
import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.util.Matchable;
import com.sigmundgranaas.forgero.core.util.Type;

import java.util.List;

public interface Upgrade extends PropertyContainer, Matchable, Identifiable {
    static Upgrade of(Composite composite) {
        return new CompositeUpgrade(composite);
    }

    static Upgrade of(String name, Type type, List<Property> properties) {
        return new SimpleUpgrade(name, type, properties);
    }

}
