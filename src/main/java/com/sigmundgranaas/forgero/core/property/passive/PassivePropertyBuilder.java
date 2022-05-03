package com.sigmundgranaas.forgero.core.property.passive;

import com.sigmundgranaas.forgero.core.property.PassivePropertyType;
import com.sigmundgranaas.forgero.core.property.PropertyPOJO;

public class PassivePropertyBuilder {
    public static PassiveProperty createPassivePropertyFromPojo(PropertyPOJO.Passive propertyPOJO) {
        if (propertyPOJO.type == PassivePropertyType.STATIC && propertyPOJO.tag.equals("GOLDEN")) {
            return new Golden();
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
