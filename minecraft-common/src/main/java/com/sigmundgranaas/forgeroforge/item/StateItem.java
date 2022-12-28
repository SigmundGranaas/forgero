package com.sigmundgranaas.forgeroforge.item;

import com.sigmundgranaas.forgeroforge.conversion.StateConverter;
import com.sigmundgranaas.forgero.property.PropertyContainer;
import com.sigmundgranaas.forgero.state.State;
import com.sigmundgranaas.forgero.type.Type;
import com.sigmundgranaas.forgero.util.match.Context;
import com.sigmundgranaas.forgero.util.match.Matchable;
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
