package com.sigmundgranaas.forgerocommon.conversion;

import com.sigmundgranaas.forgero.state.State;
import com.sigmundgranaas.forgerocommon.utils.ItemUtils;
import com.sigmundgranaas.forgerocommon.utils.StateUtils;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.Optional;

public interface StateConverter {
    static Optional<State> of(ItemStack stack) {
        return new StackToItemConverter().convert(stack);
    }

    static Optional<State> of(Item item) {
        return new ItemToStateConverter(ItemUtils::itemToStateFinder).convert(item);
    }

    static ItemStack of(State state) {
        return new StateToStackConverter(ItemUtils::itemFinder, StateUtils::containerMapper).convert(state);
    }
}
