package com.sigmundgranaas.forgero.core.property.active;

import com.sigmundgranaas.forgero.core.property.PropertyPOJO;

public class ActivePropertyBuilder {
    public static ActiveProperty createAttributeFromPojo(PropertyPOJO.Active activePOJO) {
        if (activePOJO.type == ActivePropertyType.BLOCK_BREAKING_PATTERN) {
            return new PatternBreaking(activePOJO.pattern);
        } else {
            return new VeinBreaking(activePOJO.depth, activePOJO.tag, activePOJO.description);
        }
    }
}
