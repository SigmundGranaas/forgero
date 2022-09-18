package com.sigmundgranaas.forgero;

import com.sigmundgranaas.forgero.resource.ResourceListener;
import com.sigmundgranaas.forgero.resource.data.v2.data.DataResource;
import com.sigmundgranaas.forgero.state.Composite;
import com.sigmundgranaas.forgero.state.State;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.sigmundgranaas.forgero.util.Identifiers.CREATE_IDENTIFIER;

public class Registry {
    public static ResourceRegistry<State> STATES;
    public static Set<String> CONTAINERS;
    public static Set<String> COMPOSITES;

    public static ResourceListener<Map<String, State>> stateListener() {
        return (resources, tree, idMapper) -> {
            if (STATES == null) {
                STATES = ResourceRegistry.of(resources, tree);
            }
        };
    }

    public static ResourceListener<List<DataResource>> containerListener() {
        return (resources, tree, idMapper) -> {
            if (CONTAINERS == null) {
                CONTAINERS = resources.stream()
                        .filter(data -> data.container().isPresent())
                        .filter(data -> data.container().get().getType().equals(CREATE_IDENTIFIER))
                        .map(data -> idMapper.get(data.identifier()))
                        .collect(Collectors.toSet());
            }
        };
    }

    public static ResourceListener<Map<String, State>> compositeListener() {
        return (resources, tree, idMapper) -> {
            if (COMPOSITES == null) {
                COMPOSITES = resources.values()
                        .stream()
                        .filter(Composite.class::isInstance)
                        .map(State::identifier)
                        .collect(Collectors.toSet());
            }
        };
    }
}
