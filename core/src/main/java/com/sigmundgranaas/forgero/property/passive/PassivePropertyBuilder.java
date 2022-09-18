package com.sigmundgranaas.forgero.property.passive;

import com.sigmundgranaas.forgero.resource.data.v1.pojo.PropertyPojo;

public class PassivePropertyBuilder {
    public static PassiveProperty createPassivePropertyFromPojo(PropertyPojo.Passive propertyPOJO) {
        if (propertyPOJO.type == PassivePropertyType.STATIC) {
            return new StaticProperty(StaticPassiveType.valueOf(propertyPOJO.tag.toUpperCase()));
        } else {
            return new LeveledProperty(LeveledPassiveType.MAGNETIC);
        }
    }
}
