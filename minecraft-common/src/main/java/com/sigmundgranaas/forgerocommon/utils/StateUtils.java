package com.sigmundgranaas.forgerocommon.utils;

import com.sigmundgranaas.forgero.ForgeroStateRegistry;
import com.sigmundgranaas.forgero.registry.StateFinder;
import com.sigmundgranaas.forgero.state.State;
import net.minecraft.util.Identifier;

import java.util.Optional;

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

    public static Optional<String> containerToStateMapper(String id) {
        return Optional.ofNullable(ForgeroStateRegistry.CONTAINER_TO_STATE.get(id));
    }
}
