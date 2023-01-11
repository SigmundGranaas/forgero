package com.sigmundgranaas.forgero.minecraft.common.conversion;

import com.sigmundgranaas.forgero.minecraft.common.utils.StateUtils;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.minecraft.common.utils.ItemUtils;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public interface StateConverter {
    static Map<ItemStack, State> cachedStates = new ConcurrentHashMap<>();

    static Optional<State> of(ItemStack stack) {
        State state = cachedStates.get(stack);
        if(state == null){
           var converted = new StackToItemConverter().convert(stack);
            converted.ifPresent(value -> cachedStates.put(stack, value));
           return converted;
        }
        return Optional.of(state);
    }

    static Optional<State> of(Item item) {
        return new ItemToStateConverter(ItemUtils::itemToStateFinder).convert(item);
    }

    static ItemStack of(State state) {
        return new StateToStackConverter(ItemUtils::itemFinder, StateUtils::containerMapper).convert(state);
    }
}
