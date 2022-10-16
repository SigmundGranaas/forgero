package com.sigmundgranaas.forgero.item.items;

import com.sigmundgranaas.forgero.item.StateItem;
import com.sigmundgranaas.forgero.state.State;
import net.minecraft.item.Item;

public class DefaultStateItem extends Item implements StateItem {
    private final State defaultState;

    public DefaultStateItem(Settings settings, State defaultState) {
        super(settings);
        this.defaultState = defaultState;
    }

    @Override
    public State defaultState() {
        return defaultState;
    }
}
