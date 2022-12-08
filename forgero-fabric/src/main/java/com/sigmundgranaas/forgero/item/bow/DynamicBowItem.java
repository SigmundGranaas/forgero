package com.sigmundgranaas.forgero.item.bow;

import com.sigmundgranaas.forgero.item.StateItem;
import com.sigmundgranaas.forgero.state.State;
import net.minecraft.item.BowItem;

public class DynamicBowItem extends BowItem implements StateItem {
    private final State defaultState;

    public DynamicBowItem(Settings settings, State defaultState) {
        super(settings);
        this.defaultState = defaultState;
    }

    @Override
    public State defaultState() {
        return defaultState;
    }
}
