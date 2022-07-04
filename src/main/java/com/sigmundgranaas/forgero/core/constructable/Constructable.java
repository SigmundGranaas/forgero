package com.sigmundgranaas.forgero.core.constructable;

import com.sigmundgranaas.forgero.core.property.PropertyContainer;

import java.util.List;

public interface Constructable extends PropertyContainer {
    List<Construct<?>> getConstructs();
}
