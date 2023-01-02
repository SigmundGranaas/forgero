package com.sigmundgranaas.forgero.minecraft.common.conversion;

import com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2.CompoundParser;
import com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2.NbtConstants;
import com.sigmundgranaas.forgero.core.state.State;
import net.minecraft.item.ItemStack;

import java.util.Optional;

public class StackToItemConverter implements Converter<ItemStack, Optional<State>> {

    @Override
    public Optional<State> convert(ItemStack stack) {
        if (stack.hasNbt() && stack.getOrCreateNbt().contains(NbtConstants.FORGERO_IDENTIFIER)) {
            return CompoundParser.STATE_PARSER.parse(stack.getOrCreateNbt().getCompound(NbtConstants.FORGERO_IDENTIFIER));
        } else {
            return StateConverter.of(stack.getItem());
        }
    }
}
