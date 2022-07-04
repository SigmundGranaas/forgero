package com.sigmundgranaas.forgero.core.material.material;

import com.sigmundgranaas.forgero.core.property.Property;
import com.sigmundgranaas.forgero.core.constructable.Construct;

import java.util.Collections;
import java.util.List;

public interface PrimaryMaterial extends ForgeroMaterial, Construct<PrimaryMaterial> {
    default List<Property> getPrimaryProperties() {
        return Collections.emptyList();
    }
}
