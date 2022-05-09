package com.sigmundgranaas.forgero.core.property.active;

import com.sigmundgranaas.forgero.core.property.ActivePropertyType;
import com.sigmundgranaas.forgero.core.property.Target;

public record PatternBreaking(String[] pattern, BreakingDirection direction) implements ActiveProperty {

    @Override
    public float applyAttribute(Target target, float currentAttribute) {
        return currentAttribute;
    }

    @Override
    public boolean applyCondition(Target target) {
        return true;
    }

    @Override
    public ActivePropertyType getActiveType() {
        return ActivePropertyType.BLOCK_BREAKING_PATTERN;
    }
}
