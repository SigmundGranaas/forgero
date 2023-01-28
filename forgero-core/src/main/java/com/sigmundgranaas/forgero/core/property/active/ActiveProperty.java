package com.sigmundgranaas.forgero.core.property.active;

import com.sigmundgranaas.forgero.core.property.ActivePropertyType;
import com.sigmundgranaas.forgero.core.property.Property;
import com.sigmundgranaas.forgero.core.property.Target;

public interface ActiveProperty extends Property {

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
