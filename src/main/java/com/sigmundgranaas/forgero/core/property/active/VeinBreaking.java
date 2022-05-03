package com.sigmundgranaas.forgero.core.property.active;

import com.sigmundgranaas.forgero.core.property.ActivePropertyType;

public record VeinBreaking(int depth, String tag, String description) implements ActiveProperty {
    @Override
    public ActivePropertyType getActiveType() {
        return ActivePropertyType.VEIN_MINING_PATTERN;
    }
}
