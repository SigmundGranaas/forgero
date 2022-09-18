package com.sigmundgranaas.forgero.state;

import com.sigmundgranaas.forgero.property.Property;
import com.sigmundgranaas.forgero.type.Type;

import java.util.List;

public interface Upgrade extends State {
    static Upgrade of(Composite composite) {
        return new CompositeUpgrade(composite);
    }

    static Upgrade of(String name, Type type, List<Property> properties) {
        return new SimpleUpgrade(name, type, properties);
    }

}
