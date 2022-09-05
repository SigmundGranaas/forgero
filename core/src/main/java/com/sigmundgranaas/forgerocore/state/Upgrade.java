package com.sigmundgranaas.forgerocore.state;

import com.sigmundgranaas.forgerocore.property.Property;
import com.sigmundgranaas.forgerocore.property.PropertyContainer;
import com.sigmundgranaas.forgerocore.type.Type;
import com.sigmundgranaas.forgerocore.util.Matchable;

import java.util.List;

public interface Upgrade extends PropertyContainer, Matchable, Identifiable {
    static Upgrade of(Composite composite) {
        return new CompositeUpgrade(composite);
    }

    static Upgrade of(String name, Type type, List<Property> properties) {
        return new SimpleUpgrade(name, type, properties);
    }

}
