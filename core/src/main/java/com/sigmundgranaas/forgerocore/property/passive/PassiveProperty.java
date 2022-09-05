package com.sigmundgranaas.forgerocore.property.passive;

import com.sigmundgranaas.forgerocore.property.Property;
import com.sigmundgranaas.forgerocore.property.PropertyTypes;

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
