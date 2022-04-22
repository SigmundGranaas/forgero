package com.sigmundgranaas.forgero.core.property.active;

import com.sigmundgranaas.forgero.core.property.Property;
import com.sigmundgranaas.forgero.core.property.PropertyTypes;

public interface ActiveProperty extends Property {
    @Override
    default PropertyTypes getType() {
        return PropertyTypes.ACTIVE;
    }

    ActivePropertyType getActiveType();
}
