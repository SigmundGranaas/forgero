package com.sigmundgranaas.forgero.minecraft.common.item;

import com.sigmundgranaas.forgero.minecraft.common.conversion.StateConverter;
import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.type.Type;
import com.sigmundgranaas.forgero.core.util.match.Context;
import com.sigmundgranaas.forgero.core.util.match.Matchable;
import net.minecraft.item.ItemStack;

public interface StateItem extends DynamicAttributeItem, State {
    State defaultState();

    default State dynamicState(ItemStack stack) {
        return StateConverter.of(stack).orElse(defaultState());
    }

    @Override
    default PropertyContainer dynamicProperties(ItemStack stack) {
        return dynamicState(stack);
    }

    @Override
    default PropertyContainer defaultProperties() {
        return defaultState();
    }

    default State state(ItemStack stack) {
        return StateConverter.of(stack).orElse(defaultState());
    }


    default String name() {
        return defaultState().name();
    }

    default String nameSpace() {
        return defaultState().nameSpace();
    }

    default Type type() {
        return defaultState().type();
    }

    default boolean test(Matchable match, Context context) {
        return defaultState().test(match, context);
    }


}
