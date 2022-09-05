package com.sigmundgranaas.forgerocore.property.active;

import com.sigmundgranaas.forgerocore.property.ActivePropertyType;
import com.sigmundgranaas.forgerocore.property.Property;
import com.sigmundgranaas.forgerocore.property.PropertyTypes;
import com.sigmundgranaas.forgerocore.property.Target;

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
