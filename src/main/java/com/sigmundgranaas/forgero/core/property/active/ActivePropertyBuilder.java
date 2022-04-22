package com.sigmundgranaas.forgero.core.property.active;

import com.sigmundgranaas.forgero.core.property.PropertyPOJO;

public class ActivePropertyBuilder {
    public static ActiveProperty createAttributeFromPojo(PropertyPOJO.Active activePOJO) {
        return new PatternBreaking(activePOJO.pattern);
    }
}
