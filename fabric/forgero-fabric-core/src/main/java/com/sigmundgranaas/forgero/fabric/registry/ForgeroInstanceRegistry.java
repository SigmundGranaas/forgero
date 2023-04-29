package com.sigmundgranaas.forgero.fabric.registry;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import com.sigmundgranaas.forgero.core.registry.StateCollection;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.state.StateProvider;
import com.sigmundgranaas.forgero.minecraft.common.conversion.CachedStackConverter;
import com.sigmundgranaas.forgero.minecraft.common.conversion.ItemToStateConverter;
import com.sigmundgranaas.forgero.minecraft.common.conversion.StateToStackConverter;
import com.sigmundgranaas.forgero.minecraft.common.service.StateMapper;
import com.sigmundgranaas.forgero.minecraft.common.service.StateService;
import com.sigmundgranaas.forgero.minecraft.common.utils.ItemUtils;
import com.sigmundgranaas.forgero.minecraft.common.utils.StateUtils;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;

public class ForgeroInstanceRegistry implements StateService {
	private final List<Identifier> tags;
	private final StateCollection collection;
	private final Registry<Item> itemRegistry;
	private final Map<String, String> itemToStateMap;
	private final Map<String, String> tagToStateMap;
	private final StateMapper mapper;

	public ForgeroInstanceRegistry(List<Identifier> tags, StateCollection collection, Registry<Item> itemRegistry, Map<String, String> itemToStateMap, Map<String, String> tagToStateMap, StateMapper mapper) {
		this.tags = tags;
		this.collection = collection;
		this.itemRegistry = itemRegistry;
		this.itemToStateMap = itemToStateMap;
		this.tagToStateMap = tagToStateMap;
		this.mapper = mapper;
	}

	@Override
	public Optional<State> find(String id) {
		return collection.find(id)
				.map(Supplier::get)
				.or(() -> findMappedId(id))
				.or(() -> findInTags(id));
	}

	private Optional<State> findMappedId(String id) {
		return Optional.ofNullable(itemToStateMap.get(id))
				.flatMap(collection::find)
				.map(Supplier::get);
	}

	private Optional<State> findInTags(String id) {
		try {
			return mapper.containerToState(id).flatMap(collection::find).map(Supplier::get);
		} catch (Exception e) {
			return Optional.empty();
		}
	}

	private Optional<State> findInTags(Identifier id) {
		return tags.stream()
				.map(tag -> TagKey.of(Registry.ITEM_KEY, tag))
				.filter(tag -> hasItem(tag, id))
				.map(tag -> Optional.ofNullable(tagToStateMap.get(tag.toString())))
				.flatMap(Optional::stream)
				.findFirst()
				.flatMap(tag -> find(id));
	}

	private boolean hasItem(TagKey<Item> tag, Identifier id) {
		for (RegistryEntry<Item> entry : itemRegistry.iterateEntries(tag)) {
			if (entry.matchesId(id)) {
				return true;
			}
		}
		return false;
	}

	public Optional<State> find(Item item) {
		return new ItemToStateConverter(itemRegistry, this).convert(item);
	}

	@Override
	public Collection<StateProvider> all() {
		return collection.all();
	}

	public Optional<State> find(Identifier id) {
		return find(id.toString());
	}

	public Optional<State> convert(ItemStack stack) {
		return new CachedStackConverter(this).convert(stack);
	}

	@Override
	public Optional<ItemStack> convert(State state) {
		ItemStack stack = new StateToStackConverter(ItemUtils::itemFinder, StateUtils::containerMapper).convert(state);
		return Optional.ofNullable(stack);
	}

	@Override
	public boolean isInitialized() {
		return true;
	}

	@Override
	public StateMapper getMapper() {
		return mapper;
	}
}
