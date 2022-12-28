package com.sigmundgranaas.forgerocommon.conversion;

import com.sigmundgranaas.forgero.state.State;
import net.minecraft.item.ItemStack;

import java.util.Optional;

import static com.sigmundgranaas.forgerocommon.item.nbt.v2.CompoundParser.STATE_PARSER;
import static com.sigmundgranaas.forgerocommon.item.nbt.v2.NbtConstants.FORGERO_IDENTIFIER;

public class StackToItemConverter implements Converter<ItemStack, Optional<State>> {

    @Override
    public Optional<State> convert(ItemStack stack) {
        if (stack.hasNbt() && stack.getOrCreateNbt().contains(FORGERO_IDENTIFIER)) {
            return STATE_PARSER.parse(stack.getOrCreateNbt().getCompound(FORGERO_IDENTIFIER));
        } else {
            return StateConverter.of(stack.getItem());
        }
    }
}
