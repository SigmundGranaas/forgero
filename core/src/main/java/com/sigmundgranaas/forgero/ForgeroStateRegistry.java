package com.sigmundgranaas.forgero;

import com.sigmundgranaas.forgero.resource.ResourceListener;
import com.sigmundgranaas.forgero.resource.data.v2.data.DataResource;
import com.sigmundgranaas.forgero.resource.data.v2.data.RecipeData;
import com.sigmundgranaas.forgero.state.Composite;
import com.sigmundgranaas.forgero.state.State;
import com.sigmundgranaas.forgero.type.TypeTree;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ForgeroStateRegistry {
    public static ResourceRegistry<State> STATES;
    public static Map<String, String> STATE_TO_CONTAINER;
    public static Map<String, String> CONTAINER_TO_STATE;
    public static Set<String> COMPOSITES;
    public static List<DataResource> CONSTRUCTS;
    public static TypeTree TREE;
    public static Map<String, String> ID_MAPPER;
    public static List<RecipeData> RECIPES;

    public static ResourceListener<Map<String, State>> stateListener() {
        return (resources, tree, idMapper) -> {
            if (STATES == null) {
                STATES = ResourceRegistry.of(resources, tree);
                TREE = tree;
                ID_MAPPER = idMapper;
            }
        };
    }

    public static ResourceListener<List<DataResource>> containerListener() {
        return (resources, tree, idMapper) -> {
            if (CONTAINER_TO_STATE == null) {
                var containerToState = new HashMap<String, String>();
                var stateToContainer = new HashMap<String, String>();
                resources.stream()
                        .filter(data -> data.container().isPresent())
                        .forEach(data -> {
                            String containerId = data.container().get().getId().equals("this") ? data.identifier() : data.container().get().getId();
                            String stateId = data.identifier();
                            if (idMapper.containsKey(stateId)) {
                                if (idMapper.containsKey(containerId)) {
                                    stateToContainer.put(idMapper.get(stateId), idMapper.get(containerId));
                                    containerToState.put(idMapper.get(containerId), idMapper.get(stateId));
                                } else {
                                    stateToContainer.put(idMapper.get(stateId), containerId);
                                    containerToState.put(containerId, idMapper.get(stateId));
                                }

                            } else {
                                stateToContainer.put(stateId, containerId);
                                containerToState.put(containerId, stateId);
                            }
                        });
                CONTAINER_TO_STATE = containerToState;
                STATE_TO_CONTAINER = stateToContainer;
            }
        };
    }

    public static ResourceListener<List<DataResource>> constructListener() {
        return (resources, tree, idMapper) -> {
            if (CONSTRUCTS == null) {
                CONSTRUCTS = resources.stream().filter(res -> res.construct().isPresent()).toList();
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

    public static ResourceListener<List<RecipeData>> recipeListener() {
        return (resources, tree, idMapper) -> {
            if (RECIPES == null) {
                RECIPES = resources;
            }
        };
    }
}
