package com.sigmundgranaas.forgero.core.property.active;

import com.sigmundgranaas.forgero.core.data.v1.pojo.PropertyPojo;
import com.sigmundgranaas.forgero.core.property.ActivePropertyType;

public class ActivePropertyBuilder {
    public static ActiveProperty createAttributeFromPojo(PropertyPojo.Active activePOJO) {
        if (activePOJO.type == ActivePropertyType.BLOCK_BREAKING_PATTERN) {
            BreakingDirection direction = activePOJO.direction == null ? BreakingDirection.ANY : activePOJO.direction;
            if (activePOJO.tag != null) {
                return new TaggedPatternBreaking(activePOJO.pattern, direction, activePOJO.tag);
            }
            return new PatternBreaking(activePOJO.pattern, direction);
        } else {
            return new VeinBreaking(activePOJO.depth, activePOJO.tag, activePOJO.description);
        }
    }
}
