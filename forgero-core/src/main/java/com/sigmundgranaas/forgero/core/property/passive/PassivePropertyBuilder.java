package com.sigmundgranaas.forgero.core.property.passive;

import com.sigmundgranaas.forgero.core.resource.data.PropertyPojo;

public class PassivePropertyBuilder {
    public static PassiveProperty createPassivePropertyFromPojo(PropertyPojo.Passive propertyPOJO) {
        if (propertyPOJO.type == PassivePropertyType.STATIC) {
            return new StaticProperty(StaticPassiveType.valueOf(propertyPOJO.tag.toUpperCase()));
        } else {
            if (propertyPOJO.tag.equals("EMISSIVE")) {
                return new LeveledProperty(LeveledPassiveType.EMISSIVE);
            }
           else if (propertyPOJO.tag.equals("MAGNETIC")) {
                return new LeveledProperty(LeveledPassiveType.MAGNETIC);
            }
           else  if(propertyPOJO.tag.equals("ENDER_TELEPORT")){
                return new LeveledProperty(LeveledPassiveType.ENDER_TELEPORT);
            }
            return new LeveledProperty(LeveledPassiveType.EMISSIVE);
        }
    }
}
