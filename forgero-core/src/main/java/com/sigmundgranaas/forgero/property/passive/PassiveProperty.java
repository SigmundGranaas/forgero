package com.sigmundgranaas.forgero.property.passive;

import com.sigmundgranaas.forgero.property.Property;
import com.sigmundgranaas.forgero.property.PropertyTypes;

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
