package com.sigmundgranaas.forgero.utils;

import com.sigmundgranaas.forgero.state.State;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.Optional;

import static com.sigmundgranaas.forgero.utils.StateUtils.stateFinder;

public class ItemUtils {
    public static Optional<Item> itemFinder(Identifier id) {
        if (Registry.ITEM.containsId(id)) {
            return Optional.of(Registry.ITEM.get(id));
        }
        return Optional.empty();
    }

    public static Identifier idFinder(Item id) {
        return Registry.ITEM.getId(id);
    }

    public static Optional<State> itemToStateFinder(Item item) {
        return stateFinder(idFinder(item).toString());
    }

}
