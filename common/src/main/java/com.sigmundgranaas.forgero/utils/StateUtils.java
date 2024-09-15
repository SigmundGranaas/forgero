package com.sigmundgranaas.forgero.utils;

import java.util.Optional;

import com.sigmundgranaas.forgero.core.ForgeroStateRegistry;
import com.sigmundgranaas.forgero.core.registry.StateFinder;
import com.sigmundgranaas.forgero.core.state.State;

import net.minecraft.util.Identifier;

public class StateUtils {
	public static StateFinder finder = ForgeroStateRegistry.stateFinder();

	public static Optional<State> stateFinder(String id) {
		if (finder.find(id).isPresent()) {
			return finder.find(id);
		} else if (containerMapper(id).isPresent()) {
			return containerToStateMapper(id).flatMap(finder::find);
		}
		return Optional.empty();
	}

	public static Optional<Identifier> containerMapper(String id) {
		return Optional.ofNullable(ForgeroStateRegistry.STATE_TO_CONTAINER.get(id)).map(Identifier::new);
	}

	public static Identifier defaultedContainerMapper(State state) {
		return containerMapper(state.identifier()).orElse(new Identifier(state.identifier()));
	}

	public static Optional<String> containerToStateMapper(String id) {
		return Optional.ofNullable(ForgeroStateRegistry.CONTAINER_TO_STATE.get(id));
	}
}
