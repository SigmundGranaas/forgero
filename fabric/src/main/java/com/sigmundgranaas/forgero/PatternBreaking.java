package com.sigmundgranaas.forgero;

import com.sigmundgranaas.forgero.property.ActivePropertyType;
import com.sigmundgranaas.forgero.property.Target;
import com.sigmundgranaas.forgero.property.active.ActiveProperty;
import com.sigmundgranaas.forgero.property.active.BreakingDirection;
import net.minecraft.block.BlockState;

public class PatternBreaking implements ActiveProperty {
    private final String[] pattern;
    private final BreakingDirection direction;

    public PatternBreaking(String[] pattern, BreakingDirection direction) {
        this.pattern = pattern;
        this.direction = direction;
    }

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

    public boolean checkBlock(BlockState state) {
        return true;
    }

    public String[] getPattern() {
        return pattern;
    }

    public BreakingDirection getDirection() {
        return direction;
    }
}
