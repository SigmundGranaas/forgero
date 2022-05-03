package com.sigmundgranaas.forgero.core.property.passive;

import com.sigmundgranaas.forgero.core.property.PassivePropertyType;
import com.sigmundgranaas.forgero.core.property.Property;
import com.sigmundgranaas.forgero.core.property.PropertyTypes;

public interface PassiveProperty extends Property {

    @Override
    default PropertyTypes getType() {
        return PropertyTypes.PASSIVE;
    }

    PassivePropertyType getPassiveType();

    default boolean isToggleable() {
        return false;
    }
}
