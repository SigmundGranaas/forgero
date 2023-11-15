package com.sigmundgranaas.forgero.fabric.initialization.registrar;


import static com.sigmundgranaas.forgero.core.identifier.Common.ELEMENT_SEPARATOR;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.core.ForgeroStateRegistry;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.Rarity;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.state.StateProvider;
import com.sigmundgranaas.forgero.core.type.Type;
import com.sigmundgranaas.forgero.fabric.item.StateToItemConverter;
import com.sigmundgranaas.forgero.minecraft.common.registry.registrar.Registrar;
import com.sigmundgranaas.forgero.minecraft.common.service.StateService;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;

/**
 * A class to handle registration of states.
 */
public class StateItemRegistrar implements Registrar {
	private final StateService service;

	public StateItemRegistrar(StateService service) {
		this.service = service;
	}

	private Map<String, Integer> generateSortingMap() {
		var sortingMap = new HashMap<String, Integer>();
		service.all().stream().map(Supplier::get)
				.filter(state -> !state.test(Type.WEAPON) && !state.test(Type.TOOL))
				.forEach(state -> sortingMap.compute(StateAttributes.getMaterialName(state),
						(key, value) -> value == null || StateAttributes.getRarity(state) > value ? StateAttributes.getRarity(state) : value));
		return sortingMap;
	}

	private Stream<StateProvider> getValidStates(Registry<Item> registry) {
		return ForgeroStateRegistry.CREATE_STATES.stream()
				.filter(state -> !registry.containsId(new Identifier(service.getMapper().stateToContainer(state.get().identifier()).toString())))
				.filter(state -> !registry.containsId(new Identifier(state.get().identifier())));
	}

	private void registerState(StateProvider state) {
		try {
			var converter = StateToItemConverter.of(state);
			Identifier identifier = converter.id();
			var item = converter.convert();
			Registry.register(Registries.ITEM, identifier, item);
		} catch (InvalidIdentifierException e) {
			Forgero.LOGGER.error("Invalid identifier: {}", state.get().identifier());
			Forgero.LOGGER.error(e);
		}
	}

	@Override
	public void registerItem(Registry<Item> registry) {
		var sortingMap = generateSortingMap();
		getValidStates(registry)
				.sorted(new StateComparator(sortingMap))
				.forEach(this::registerState);
	}

	/**
	 * A utility class to extract and manipulate attributes from State objects.
	 */
	static class StateAttributes {
		/**
		 * Retrieves the material name from a State object.
		 *
		 * @param state The state object.
		 * @return The material name.
		 */
		static String getMaterialName(State state) {
			var elements = state.name().split(ELEMENT_SEPARATOR);
			if (elements.length > 1) {
				if (elements[1].equals("schematic") || elements[1].equals("gem")) {
					return elements[1];
				} else {
					return elements[0];
				}
			} else {
				return state.name();
			}
		}

		/**
		 * Retrieves the rarity attribute from a State object.
		 *
		 * @param state The state object.
		 * @return The rarity value.
		 */
		static int getRarity(State state) {
			return (int) state.stream().applyAttribute(Rarity.KEY);
		}
	}

	/**
	 * A comparator class to compare two State objects based on a given ordering map.
	 */
	static class StateComparator implements Comparator<Supplier<State>> {
		private final Map<String, Integer> orderingMap;

		public StateComparator(Map<String, Integer> orderingMap) {
			this.orderingMap = orderingMap;
		}

		private int getOrderingFromState(State state) {
			var name = StateAttributes.getMaterialName(state);
			int rarity = StateAttributes.getRarity(state);
			return orderingMap.getOrDefault(name, rarity);
		}

		@Override
		public int compare(Supplier<State> state1, Supplier<State> state2) {
			State element1 = state1.get();
			State element2 = state2.get();

			int elementOrdering = getOrderingFromState(element1) - getOrderingFromState(element2);
			int nameOrdering = StateAttributes.getMaterialName(element1).compareTo(StateAttributes.getMaterialName(element2));

			if (elementOrdering != 0) {
				return elementOrdering;
			} else if (nameOrdering != 0) {
				return nameOrdering;
			} else {
				return StateAttributes.getRarity(element1) - StateAttributes.getRarity(element2);
			}
		}
	}
}
