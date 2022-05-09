package com.sigmundgranaas.forgero.core.property;

import java.util.List;

public interface PropertyContainer {
    List<Property> getProperties(Target target);
}
