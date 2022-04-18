package com.sigmundgranaas.forgero.core.property;

import com.sigmundgranaas.forgero.core.property.attribute.Target;

import java.util.List;

public interface PropertyContainer {
    List<Property> getProperties(Target target);
}
