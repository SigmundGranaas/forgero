package com.sigmundgranaas.forgero.core.property.passive;

import com.sigmundgranaas.forgero.core.property.PassivePropertyType;
import com.sigmundgranaas.forgero.core.property.PropertyPOJO;

public class PassivePropertyBuilder {
    public static PassiveProperty createPassivePropertyFromPojo(PropertyPOJO.Passive propertyPOJO) {
        if (propertyPOJO.type == PassivePropertyType.STATIC) {
            return new StaticProperty(StaticPassiveType.valueOf(propertyPOJO.tag.toUpperCase()));
        } else {
            return new PassiveProperty() {
                @Override
                public PassivePropertyType getPassiveType() {
                    return PassivePropertyType.STATIC;
                }
            };
        }
    }
}
