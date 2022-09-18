package com.sigmundgranaas.forgero.material.material;

import com.sigmundgranaas.forgero.property.Property;

import java.util.Collections;
import java.util.List;

public interface PrimaryMaterial extends ForgeroMaterial {
    default List<Property> getPrimaryProperties() {
        return Collections.emptyList();
    }
}
