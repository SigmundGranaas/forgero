package com.sigmundgranaas.forgeroforge.utils;

import com.sigmundgranaas.forgero.state.State;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.Optional;

import static com.sigmundgranaas.forgero.ForgeroStateRegistry.CONTAINER_TO_STATE;
import static com.sigmundgranaas.forgeroforge.utils.StateUtils.stateFinder;

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
        var id = idFinder(item).toString();
        return stateFinder(id).or(() -> Optional.ofNullable(CONTAINER_TO_STATE.get(id)).flatMap(StateUtils::stateFinder));
    }

}
