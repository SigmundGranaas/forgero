package com.sigmundgranaas.forgero.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.sigmundgranaas.forgero.core.registry.StateCollection;
import com.sigmundgranaas.forgero.core.registry.StateFinder;
import com.sigmundgranaas.forgero.core.registry.impl.ReloadableStateRegistry;
import com.sigmundgranaas.forgero.core.resource.ResourceListener;
import com.sigmundgranaas.forgero.core.resource.data.v2.data.DataResource;
import com.sigmundgranaas.forgero.core.resource.data.v2.data.RecipeData;
import com.sigmundgranaas.forgero.core.state.Composite;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.state.StateProvider;
import com.sigmundgranaas.forgero.core.type.TypeTree;

public class ForgeroStateRegistry {
	@Nullable
	public static StateCollection STATES;
	public static List<StateProvider> CREATE_STATES;
	public static Map<String, String> STATE_TO_CONTAINER;
	public static List<String> TAGS;
	public static Map<String, String> CONTAINER_TO_STATE;
	public static Set<String> COMPOSITES;
	public static List<DataResource> CONSTRUCTS;
	public static TypeTree TREE;
	public static Map<String, String> ID_MAPPER;
	public static List<RecipeData> RECIPES;

	public static StateFinder stateFinder() {
		return (String id) ->
		{
			if (isState().apply(id) && STATES != null) {
				return STATES.find(id).map(Supplier::get);
			} else {
				return Optional.empty();
			}
		};
	}

	public static Function<String, Boolean> isState() {
		return (id) -> (STATES != null && STATES.contains(id)) || CONTAINER_TO_STATE.containsKey(id);
	}

	public static ResourceListener<Map<String, State>> stateListener() {
		return (resources, tree, idMapper) -> {
			if (STATES == null) {
				var registry = new ReloadableStateRegistry();
				resources.values().forEach(registry::register);
				resources.values().forEach(state -> tree.find(state.type()).ifPresent(node -> node.addResource(state, State.class)));
				STATES = registry;
				TREE = tree;
				ID_MAPPER = idMapper;
			} else if (STATES instanceof ReloadableStateRegistry registry) {
				resources.values().forEach(registry::update);
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

	public static ResourceListener<List<String>> createStateListener() {
		return (resources, tree, idMapper) -> {
			if (CREATE_STATES == null && STATES != null) {
				CREATE_STATES = resources.stream()
						.map(STATES::find)
						.flatMap(Optional::stream)
						.filter(StateProvider.class::isInstance)
						.map(StateProvider.class::cast)
						.toList();
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

	public static ResourceListener<List<String>> tagListener() {
		return (resources, tree, idMapper) -> {
			if (TAGS == null) {
				TAGS = resources;
			}
		};
	}
}
