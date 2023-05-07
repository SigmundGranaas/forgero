package com.sigmundgranaas.forgero.minecraft.common.utils;

import static com.sigmundgranaas.forgero.core.ForgeroStateRegistry.CONTAINER_TO_STATE;

import java.util.Optional;

import com.sigmundgranaas.forgero.core.state.State;

import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;


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

	public static boolean exists(String id) {
		return Registry.ITEM.containsId(new Identifier(id));
	}

	public static boolean exists(State state) {
		return Registry.ITEM.containsId(StateUtils.defaultedContainerMapper(state));
	}

	public static Optional<State> itemToStateFinder(Item item) {
		var id = idFinder(item).toString();
		return StateUtils.stateFinder(id).or(() -> Optional.ofNullable(CONTAINER_TO_STATE.get(id)).flatMap(StateUtils::stateFinder));
	}

}
