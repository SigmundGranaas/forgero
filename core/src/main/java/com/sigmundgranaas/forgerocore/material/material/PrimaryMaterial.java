package com.sigmundgranaas.forgerocore.material.material;

import com.sigmundgranaas.forgerocore.property.Property;

import java.util.Collections;
import java.util.List;

public interface PrimaryMaterial extends ForgeroMaterial {
    default List<Property> getPrimaryProperties() {
        return Collections.emptyList();
    }
}
