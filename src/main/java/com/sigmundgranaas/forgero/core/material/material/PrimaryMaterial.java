package com.sigmundgranaas.forgero.core.material.material;

import com.sigmundgranaas.forgero.core.property.Property;

import java.util.Collections;
import java.util.List;

public interface PrimaryMaterial extends ForgeroMaterial {
    default List<Property> getPrimaryProperties() {
        return Collections.emptyList();
    }
}
