package com.sigmundgranaas.forgero.fabric.registry;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import com.sigmundgranaas.forgero.core.registry.StateCollection;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.minecraft.common.conversion.CachedConverter;
import com.sigmundgranaas.forgero.minecraft.common.service.StateService;

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
	private CachedConverter converter;

	public ForgeroInstanceRegistry(List<Identifier> tags, StateCollection collection) {
		this(tags, collection, Registry.ITEM);
		this.converter = CachedConverter.of(this);
	}

	public ForgeroInstanceRegistry(List<Identifier> tags, StateCollection collection, Registry<Item> itemRegistry) {
		this.tags = tags;
		this.collection = collection;
		this.itemRegistry = itemRegistry;
	}

	@Override
	public Optional<State> find(String id) {
		return collection.find(id).map(Supplier::get).or(() -> findInTags(id));
	}

	private Optional<State> findInTags(String id) {
		try {
			Identifier identifier = new Identifier(id);
			return findInTags(identifier);
		} catch (Exception e) {
			return Optional.empty();
		}
	}

	private Optional<State> findInTags(Identifier id) {
		return tags.stream()
				.map(tag -> TagKey.of(Registry.ITEM_KEY, tag))
				.filter(tag -> hasItem(tag, id))
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
		return converter.of(item);
	}

	public Optional<State> find(Identifier id) {
		return find(id.toString());
	}

	public Optional<State> convert(ItemStack stack) {
		return converter.of(stack);
	}

	@Override
	public boolean isInitialized() {
		return true;
	}
}
