package com.sigmundgranaas.forgero.minecraft.common.utils;

import com.sigmundgranaas.forgero.core.state.State;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.Optional;

import static com.sigmundgranaas.forgero.core.ForgeroStateRegistry.CONTAINER_TO_STATE;

public class ItemUtils {
    public static Optional<Item> itemFinder(Identifier id) {
        if (Registries.ITEM.containsId(id)) {
            return Optional.of(Registries.ITEM.get(id));
        }
        return Optional.empty();
    }

    public static Identifier idFinder(Item id) {
        return Registries.ITEM.getId(id);
    }

	public static Optional<State> itemToStateFinder(Item item) {
		var id = idFinder(item).toString();
		return StateUtils.stateFinder(id).or(() -> Optional.ofNullable(CONTAINER_TO_STATE.get(id)).flatMap(StateUtils::stateFinder));
	}

}
