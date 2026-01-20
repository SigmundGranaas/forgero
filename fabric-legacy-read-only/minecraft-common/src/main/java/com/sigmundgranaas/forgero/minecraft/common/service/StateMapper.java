package com.sigmundgranaas.forgero.minecraft.common.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;


public class StateMapper {
	private final Map<String, String> containerToStateMap;
	private final Map<String, String> stateToContainerMap;
	private final Map<String, String> stateToTagMap;
	private final Map<String, String> tagToStateMap;

	public StateMapper(Map<String, String> containerToStateMap, Map<String, String> stateToContainerMap, Map<String, String> stateToTagMap, Map<String, String> tagToStateMap) {
		this.containerToStateMap = containerToStateMap;
		this.stateToContainerMap = stateToContainerMap;
		this.stateToTagMap = stateToTagMap;
		this.tagToStateMap = tagToStateMap;
	}

	public StateMapper() {
		this.containerToStateMap = new HashMap<>();
		this.stateToContainerMap = new HashMap<>();
		this.stateToTagMap = new HashMap<>();
		this.tagToStateMap = new HashMap<>();
	}

	public Optional<String> containerToState(String id) {
		Optional<String> identifier = Optional.ofNullable(containerToStateMap.get(id));
		return identifier.or(() -> Optional.ofNullable(tagToStateMap.get(findTagIdFromContainer(new Identifier(id)).map(Identifier::toString).orElse(""))));
	}

	public Optional<String> containerToState(Identifier id) {
		return containerToState(id.toString());
	}

	public Identifier stateToContainer(String id) {
		Optional<String> containerId = Optional.ofNullable(stateToContainerMap.get(id));
		if (containerId.isPresent()) {
			return new Identifier(containerId.get());
		} else {
			Optional<String> containerTag = Optional.ofNullable(stateToTagMap.get(id));
			if (containerTag.isPresent()) {
				Identifier tagId = new Identifier(containerTag.get());
				TagKey<Item> tag = TagKey.of(Registries.ITEM.getKey(), tagId);
				for (RegistryEntry<Item> item : Registries.ITEM.iterateEntries(tag)) {
					return Registries.ITEM.getId(item.value());
				}
				return tagId;
			} else {
				return new Identifier(id);
			}
		}
	}

	public Optional<Identifier> findTagIdFromContainer(Identifier id) {
		for (String tag : stateToTagMap.values()) {
			Identifier identifier = new Identifier(tag);
			TagKey<Item> tagKey = TagKey.of(Registries.ITEM.getKey(), identifier);
			for (RegistryEntry<Item> item : Registries.ITEM.iterateEntries(tagKey)) {
				if (Registries.ITEM.getId(item.value()).equals(id)) {
					return Optional.of(identifier);
				}
			}

		}
		return Optional.empty();
	}

	public Optional<Identifier> stateToTag(String id) {
		return Optional.ofNullable(stateToTagMap.get(id)).map(Identifier::new);
	}
}
