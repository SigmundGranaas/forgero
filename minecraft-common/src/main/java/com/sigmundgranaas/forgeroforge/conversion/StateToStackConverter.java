package com.sigmundgranaas.forgeroforge.conversion;

import com.sigmundgranaas.forgeroforge.item.nbt.v2.StateEncoder;
import com.sigmundgranaas.forgero.state.State;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

import java.util.Optional;
import java.util.function.Function;

import static com.sigmundgranaas.forgeroforge.item.nbt.v2.NbtConstants.FORGERO_IDENTIFIER;

public class StateToStackConverter implements Converter<State, ItemStack> {
    private final Function<Identifier, Optional<Item>> itemFinder;

    private final Function<String, Optional<Identifier>> containerMapper;

    public StateToStackConverter(Function<Identifier, Optional<Item>> itemFinder, Function<String, Optional<Identifier>> containerMapper) {
        this.itemFinder = itemFinder;
        this.containerMapper = containerMapper;
    }

    @Override
    public ItemStack convert(State state) {
        NbtCompound compound = new NbtCompound();
        compound.put(FORGERO_IDENTIFIER, StateEncoder.ENCODER.encode(state));
        Optional<Item> convertedStackItem = Optional.of(state.identifier())
                .map(Identifier::new)
                .flatMap(itemFinder)
                .or(() -> containerMapper.apply(state.identifier()).flatMap(itemFinder));

        if (convertedStackItem.isPresent()) {
            var stack = new ItemStack(convertedStackItem.get());
            stack.setNbt(compound);
            return stack;
        }
        return ItemStack.EMPTY;
    }
}
