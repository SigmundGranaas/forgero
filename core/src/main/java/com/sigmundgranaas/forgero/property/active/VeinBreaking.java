package com.sigmundgranaas.forgero.property.active;

import com.sigmundgranaas.forgero.property.ActivePropertyType;

public record VeinBreaking(int depth, String tag, String description) implements ActiveProperty {
    @Override
    public ActivePropertyType getActiveType() {
        return ActivePropertyType.VEIN_MINING_PATTERN;
    }
}
