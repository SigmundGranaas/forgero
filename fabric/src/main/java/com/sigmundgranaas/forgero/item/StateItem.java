package com.sigmundgranaas.forgero.item;

import com.sigmundgranaas.forgero.conversion.StateConverter;
import com.sigmundgranaas.forgero.item.items.DynamicAttributeItem;
import com.sigmundgranaas.forgero.property.PropertyContainer;
import com.sigmundgranaas.forgero.state.State;
import net.minecraft.item.ItemStack;

public interface StateItem extends DynamicAttributeItem {
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
}
