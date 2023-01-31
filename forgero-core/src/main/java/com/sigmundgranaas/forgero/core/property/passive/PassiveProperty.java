package com.sigmundgranaas.forgero.core.property.passive;

import com.sigmundgranaas.forgero.core.property.Property;

public interface PassiveProperty extends Property {


    PassivePropertyType getPassiveType();

    default boolean isToggleable() {
        return false;
    }
}
