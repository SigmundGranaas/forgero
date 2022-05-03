package com.sigmundgranaas.forgero.core.property.passive;

import com.sigmundgranaas.forgero.core.property.PassivePropertyType;

public class Golden implements PassiveProperty, Static {
    @Override
    public PassivePropertyType getPassiveType() {
        return PassivePropertyType.STATIC;
    }

    @Override
    public StaticPassiveType getStaticType() {
        return StaticPassiveType.GOLDEN;
    }
}
