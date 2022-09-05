package com.sigmundgranaas.forgerocore.material.material;

import com.sigmundgranaas.forgerocore.property.Property;

import java.util.Collections;
import java.util.List;

public interface SecondaryMaterial extends ForgeroMaterial {
    default List<Property> getSecondaryProperties() {
        return Collections.emptyList();
    }
}
