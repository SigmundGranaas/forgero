package com.sigmundgranaas.forgero.fabric.registry;

import static com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2.NbtConstants.FORGERO_IDENTIFIER;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
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

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
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
		Function<String, Optional<Identifier>> mapFn = (String id) -> Optional.of(mapper.stateToContainer(id));
		ItemStack stack = new StateToStackConverter(ItemUtils::itemFinder, mapFn).convert(state);
		return Optional.ofNullable(stack);
	}

	@Override
	public ItemStack update(State updated, ItemStack stack) {
		ItemStack copy = stack.copy();
		Optional<State> stackConverted = convert(stack);
		Optional<ItemStack> updatedStack = convert(updated);
		// Checking if both can be converted successfully
		if (updatedStack.isPresent() && stackConverted.isPresent()) {
			// Checking if the updated stack has any changes to the default one
			// Checking that the stack to update has the same base state as the target ItemStack
			if (updatedStack.get().hasNbt() && stackConverted.get().identifier().equals(updated.identifier())) {
				NbtCompound forgeroCompound = updatedStack.get().getOrCreateNbt().getCompound(FORGERO_IDENTIFIER);
				copy.getOrCreateNbt().put(FORGERO_IDENTIFIER, forgeroCompound);
			}
		}
		return copy;
	}

	@Override
	public boolean isInitialized() {
		return true;
	}

	@Override
	public StateMapper getMapper() {
		return mapper;
	}

	@Override
	public StateService uncached() {
		return new UncachedStateService(this);
	}
}
