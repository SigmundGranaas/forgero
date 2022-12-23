package com.sigmundgranaas.forgero.conversion;

import com.sigmundgranaas.forgero.ForgeroStateRegistry;
import com.sigmundgranaas.forgero.registry.StateFinder;
import com.sigmundgranaas.forgero.state.State;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.Optional;

public interface StateConverter {
    StateFinder finder = ForgeroStateRegistry.stateFinder();

    static Optional<State> of(ItemStack stack) {
        return new StackToItemConverter().convert(stack);
    }

    static Optional<State> of(Item item) {
        return new ItemToStateConverter(StateConverter::itemToStateFinder).convert(item);
    }

    static ItemStack of(State state) {
        return new StateToStackConverter(StateConverter::itemFinder, StateConverter::containerMapper).convert(state);
    }

    static Optional<Item> itemFinder(Identifier id) {
        if (Registry.ITEM.containsId(id)) {
            return Optional.of(Registry.ITEM.get(id));
        }
        return Optional.empty();
    }

    static Identifier idFinder(Item id) {
        return Registry.ITEM.getId(id);
    }

    static Optional<State> itemToStateFinder(Item item) {
        return stateFinder(idFinder(item).toString());
    }

    static Optional<State> stateFinder(String id) {
        if (finder.get(id).isPresent()) {
            return finder.get(id);
        } else if (containerMapper(id).isPresent()) {
            return containerToStateMapper(id).flatMap(finder::get);
        }
        return Optional.empty();
    }

    static Optional<Identifier> containerMapper(String id) {
        return Optional.ofNullable(ForgeroStateRegistry.STATE_TO_CONTAINER.get(id)).map(Identifier::new);
    }

    static Optional<String> containerToStateMapper(String id) {
        return Optional.ofNullable(ForgeroStateRegistry.CONTAINER_TO_STATE.get(id));
    }
}
