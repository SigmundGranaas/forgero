package com.sigmundgranaas.forgero.fabric.initialization.registrar;


import static com.sigmundgranaas.forgero.core.identifier.Common.ELEMENT_SEPARATOR;

import java.util.AbstractMap;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.core.ForgeroStateRegistry;
import com.sigmundgranaas.forgero.core.property.v2.ComputedAttribute;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.Rarity;
import com.sigmundgranaas.forgero.core.registry.RegistryFactory;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.state.StateProvider;
import com.sigmundgranaas.forgero.core.type.Type;
import com.sigmundgranaas.forgero.item.ItemData;
import com.sigmundgranaas.forgero.item.ItemRegistries;
import com.sigmundgranaas.forgero.registry.registrar.Registrar;
import com.sigmundgranaas.forgero.service.StateService;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;

/**
 * A class to handle registration of states.
 */
public class StateItemRegistrar implements Registrar {
	private final StateService service;
	private final RegistryFactory<StateProvider, ItemData> factory = new RegistryFactory<>(ItemRegistries.STATE_CONVERTER);
	private StateComparator comparator;
	private final Map<State, Integer> rarityCache;

	public StateItemRegistrar(StateService service) {
		this.service = service;
		rarityCache = service.all()
				.parallelStream()
				.map(Supplier::get)
				.collect(Collectors.toConcurrentMap(
						state -> state,
						state -> ComputedAttribute.of(state, Rarity.KEY).asInt()
				));
	}

	private void generateSortingMap() {
		var sortingMap = service.all().parallelStream()
				.map(Supplier::get)
				.filter(state -> !state.test(Type.WEAPON) && !state.test(Type.TOOL))
				.collect(Collectors.groupingBy(
						StateAttributes::getMaterialName,
						Collectors.mapping(
								rarityCache::get,
								Collectors.reducing(Integer.MIN_VALUE, Math::max)
						)
				));
		this.comparator = new StateComparator(sortingMap, rarityCache);
	}

	private Stream<StateProvider> getValidStates(Registry<Item> registry) {
		return ForgeroStateRegistry.CREATE_STATES.stream()
				.filter(state -> !registry.containsId(new Identifier(service.getMapper().stateToContainer(state.get().identifier()).toString())))
				.filter(state -> !registry.containsId(new Identifier(state.get().identifier())));
	}

	private void registerGroup(ItemData data) {
		Registries.ITEM_GROUP.getKey(data.group())
				.ifPresent(group -> ItemGroupEvents.modifyEntriesEvent(group)
						.register((entries) -> entries.add(new ItemStack(data.item()))));
	}

	public void registerItems(Registry<Item> registry) {
		long startTime = System.currentTimeMillis();

		generateSortingMap();

		List<StateProvider> validStates = getValidStates(registry).toList();

		List<Map.Entry<StateProvider, ItemData>> convertedItems = validStates.stream()
				.map(state -> new AbstractMap.SimpleEntry<>(state, convertState(state)))
				.filter(entry -> entry.getValue() != null)
				.sorted((e1, e2) -> comparator.compare(() -> e1.getKey().get(), () -> e2.getKey().get()))
				.collect(Collectors.toList());

		convertedItems.forEach(entry -> registerItem(entry.getValue()));

		long endTime = System.currentTimeMillis();
		long duration = endTime - startTime;

		Forgero.LOGGER.info("Registered {} items in {} ms", convertedItems.size(), duration);
	}

	private ItemData convertState(StateProvider state) {
		try {
			return factory.convert(state);
		} catch (InvalidIdentifierException e) {
			Forgero.LOGGER.error("Invalid identifier: {}", state.get().identifier(), e);
			return null;
		}
	}

	private void registerItem(ItemData data) {
		Registry.register(Registries.ITEM, data.id(), data.item());
		registerGroup(data);
	}

	static class StateAttributes {
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

		static int getRarity(State state, Map<State, Integer> map) {
			return map.getOrDefault(state, ComputedAttribute.of(state, Rarity.KEY).asInt());
		}
	}

	static class StateComparator implements Comparator<Supplier<State>> {
		private final Map<String, Integer> orderingMap;
		Map<State, Integer> rarityMap;

		public StateComparator(Map<String, Integer> orderingMap, Map<State, Integer> map) {
			this.orderingMap = orderingMap;
			this.rarityMap = map;
		}

		private int getOrderingFromState(State state) {
			var name = StateAttributes.getMaterialName(state);
			int rarity = StateAttributes.getRarity(state, rarityMap);
			return orderingMap.getOrDefault(name, rarity);
		}

		@Override
		public int compare(Supplier<State> state1, Supplier<State> state2) {
			State element1 = state1.get();
			State element2 = state2.get();

			int elementOrdering = getOrderingFromState(element1) - getOrderingFromState(element2);
			if (elementOrdering != 0) {
				return elementOrdering;
			}

			int nameOrdering = StateAttributes.getMaterialName(element1).compareTo(StateAttributes.getMaterialName(element2));
			if (nameOrdering != 0) {
				return nameOrdering;
			}

			return StateAttributes.getRarity(element1, rarityMap) - StateAttributes.getRarity(element2, rarityMap);
		}
	}
}
