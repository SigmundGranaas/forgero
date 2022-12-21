package com.sigmundgranaas.forgero.conversion;

import com.sigmundgranaas.forgero.ForgeroStateRegistry;
import com.sigmundgranaas.forgero.item.StateItem;
import com.sigmundgranaas.forgero.item.nbt.v2.StateEncoder;
import com.sigmundgranaas.forgero.state.State;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.Optional;

import static com.sigmundgranaas.forgero.item.nbt.v2.CompoundParser.STATE_PARSER;
import static com.sigmundgranaas.forgero.item.nbt.v2.NbtConstants.FORGERO_IDENTIFIER;

public interface StateConverter {
    static Optional<State> of(ItemStack stack) {
        if (stack.hasNbt() && stack.getOrCreateNbt().contains(FORGERO_IDENTIFIER)) {
            return STATE_PARSER.parse(stack.getOrCreateNbt().getCompound(FORGERO_IDENTIFIER));
        } else {
            return of(stack.getItem());
        }
    }

    static Optional<State> of(Item item) {
        String id = Registries.ITEM.getId(item).toString();
        var stateFromId = ForgeroStateRegistry.STATES.get(id);
        if (stateFromId.isPresent()) {
            return stateFromId;
        } else if (ForgeroStateRegistry.CONTAINER_TO_STATE.containsKey(id)) {
            return ForgeroStateRegistry.STATES.get(ForgeroStateRegistry.CONTAINER_TO_STATE.get(id));
        } else {
            if (item instanceof StateItem stateItem) {
                return Optional.of(stateItem.defaultState());
            }
        }
        return Optional.empty();
    }

    static ItemStack of(State state) {
        NbtCompound compound = new NbtCompound();
        compound.put(FORGERO_IDENTIFIER, StateEncoder.ENCODER.encode(state));
        if (Registries.ITEM.containsId(new Identifier(state.identifier()))) {
            var stack = new ItemStack(Registries.ITEM.get(new Identifier(state.identifier())));
            stack.setNbt(compound);
            return stack;
        } else {
            String mappedId = ForgeroStateRegistry.STATE_TO_CONTAINER.get(state.identifier());
            if (Registries.ITEM.containsId(new Identifier(mappedId))) {
                return new ItemStack(Registries.ITEM.get(new Identifier(mappedId)));
            }
        }
        return ItemStack.EMPTY;
    }

    static Optional<State> of(Identifier id) {
        return Optional.empty();
    }
}
