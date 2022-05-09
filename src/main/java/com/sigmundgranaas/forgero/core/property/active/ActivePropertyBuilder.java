package com.sigmundgranaas.forgero.core.property.active;

import com.sigmundgranaas.forgero.core.property.ActivePropertyType;
import com.sigmundgranaas.forgero.core.property.PropertyPOJO;

public class ActivePropertyBuilder {
    public static ActiveProperty createAttributeFromPojo(PropertyPOJO.Active activePOJO) {
        if (activePOJO.type == ActivePropertyType.BLOCK_BREAKING_PATTERN) {
            if (activePOJO.direction != null) {
                return new PatternBreaking(activePOJO.pattern, activePOJO.direction);
            }
            return new PatternBreaking(activePOJO.pattern, BreakingDirection.ANY);
        } else {
            return new VeinBreaking(activePOJO.depth, activePOJO.tag, activePOJO.description);
        }
    }
}
