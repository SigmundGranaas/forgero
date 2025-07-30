package com.sigmundgranaas.forgero.common.item;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.data.loading.api.data.host.HostData;
import com.sigmundgranaas.forgero.data.loading.api.data.host.IdentifierEntry;
import com.sigmundgranaas.forgero.data.pipeline.api.ForgeroDataBundle;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ItemToComponentMapper {
	private static ItemToComponentMapper INSTANCE;

	private final Map<Item, OpenIdentifier> itemToComponentMap;
	private final ImmutableList<Pair<TagKey<Item>, OpenIdentifier>> tagToComponentMap;

	private ItemToComponentMapper(Map<Item, OpenIdentifier> itemMap, ImmutableList<Pair<TagKey<Item>, OpenIdentifier>> tagMap) {
		this.itemToComponentMap = itemMap;
		this.tagToComponentMap = tagMap;
	}

	public static void initialize(ForgeroDataBundle bundle) {
		if (INSTANCE == null) {
			Map<Item, OpenIdentifier> itemMap = new HashMap<>();
			ImmutableList.Builder<Pair<TagKey<Item>, OpenIdentifier>> tagMapBuilder = ImmutableList.builder();

			for (Map.Entry<OpenIdentifier, HostData> entry : bundle.hostItemMap().entrySet()) {
				OpenIdentifier componentId = entry.getKey();
				HostData hostData = entry.getValue();

				if (hostData.identifiers() != null) {
					for (IdentifierEntry idEntry : hostData.identifiers()) {
						if ("item".equals(idEntry.type())) {
							Identifier mcId = new Identifier(idEntry.id().namespace(), idEntry.id().path());
							Item item = Registries.ITEM.get(mcId);
							if (item != Items.AIR) {
								itemMap.put(item, componentId);
							}
						} else if ("tag".equals(idEntry.type())) {
							Identifier mcId = new Identifier(idEntry.id().namespace(), idEntry.id().path());
							TagKey<Item> tagKey = TagKey.of(Registries.ITEM.getKey(), mcId);
							tagMapBuilder.add(Pair.of(tagKey, componentId));
						}
					}
				}
			}
			INSTANCE = new ItemToComponentMapper(itemMap, tagMapBuilder.build());
			LoggerFactory.getLogger(ItemToComponentMapper.class).info("Initialized ItemToComponentMapper with {} item mappings and {} tag mappings.", itemMap.size(), INSTANCE.tagToComponentMap.size());
		}
	}

	public static ItemToComponentMapper getInstance() {
		if (INSTANCE == null) {
			throw new IllegalStateException("ItemToComponentMapper has not been initialized. Call initialize() first.");
		}
		return INSTANCE;
	}

	public Optional<OpenIdentifier> getComponentId(ItemStack stack) {
		if (stack.isEmpty()) {
			return Optional.empty();
		}

		// Direct item mapping has priority
		OpenIdentifier directMapping = itemToComponentMap.get(stack.getItem());
		if (directMapping != null) {
			return Optional.of(directMapping);
		}

		// Fallback to tag mapping
		for (Pair<TagKey<Item>, OpenIdentifier> pair : tagToComponentMap) {
			if (stack.isIn(pair.getFirst())) {
				return Optional.of(pair.getSecond());
			}
		}

		return Optional.empty();
	}
}
