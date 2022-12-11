package com.sigmundgranaas.forgero.property.active;

import com.sigmundgranaas.forgero.property.ActivePropertyType;
import com.sigmundgranaas.forgero.property.Property;
import com.sigmundgranaas.forgero.property.PropertyTypes;
import com.sigmundgranaas.forgero.property.Target;

public interface ActiveProperty extends Property {
    @Override
    default PropertyTypes getType() {
        return PropertyTypes.ACTIVE;
    }

    @Override
    default float applyAttribute(Target target, float currentAttribute) {
        return currentAttribute;
    }

    @Override
    default boolean applyCondition(Target target) {
        return true;
    }

    ActivePropertyType getActiveType();
}
