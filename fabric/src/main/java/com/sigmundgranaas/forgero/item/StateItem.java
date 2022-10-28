package com.sigmundgranaas.forgero.item;

import com.sigmundgranaas.forgero.conversion.StateConverter;
import com.sigmundgranaas.forgero.state.State;
import net.minecraft.item.ItemStack;

public interface StateItem {
    State defaultState();

    default State state(ItemStack stack) {
        return StateConverter.of(stack).orElse(defaultState());
    }
}
