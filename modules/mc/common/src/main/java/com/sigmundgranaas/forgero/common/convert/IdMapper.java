package com.sigmundgranaas.forgero.common.convert;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.data.loading.api.data.host.HostData;
import com.sigmundgranaas.forgero.data.loading.api.data.host.IdentifierEntry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Handles stateless, bidirectional mapping between Minecraft Identifiers (for Items and Tags)
 * and Forgero OpenIdentifiers (for Components).
 * This class is initialized with data mappings and provides fast lookups.
 */
public class IdMapper {
	private final Map<Item, OpenIdentifier> itemToComponentIdMap;
	private final Map<OpenIdentifier, Identifier> componentIdToItemIdMap;
	private final ImmutableList<Pair<TagKey<Item>, OpenIdentifier>> tagToComponentIdMap;

	public IdMapper(Map<OpenIdentifier, HostData> hostItemMap) {
		Map<Item, OpenIdentifier> itemMap = new HashMap<>();
		Map<OpenIdentifier, Identifier> reverseItemMap = new HashMap<>();
		ImmutableList.Builder<Pair<TagKey<Item>, OpenIdentifier>> tagMapBuilder = ImmutableList.builder();

		for (Map.Entry<OpenIdentifier, HostData> entry : hostItemMap.entrySet()) {
			OpenIdentifier componentId = entry.getKey();
			HostData hostData = entry.getValue();

			// Add reverse mapping for items that are created from a component
			if (hostData.create() != null) {
				reverseItemMap.putIfAbsent(componentId, new Identifier(hostData.create().id().namespace(), hostData.create().id().path()));
			}

			if (hostData.identifiers() != null) {
				for (IdentifierEntry idEntry : hostData.identifiers()) {
					Identifier mcId = new Identifier(idEntry.id().namespace(), idEntry.id().path());
					if ("item".equals(idEntry.type())) {
						Item item = Registries.ITEM.get(mcId);
						if (item != Items.AIR) {
							itemMap.put(item, componentId);
							// Only add to reverse map if it's the first one, to ensure deterministic reverse mapping for mapped items
							reverseItemMap.putIfAbsent(componentId, mcId);
						}
					} else if ("tag".equals(idEntry.type())) {
						TagKey<Item> tagKey = TagKey.of(Registries.ITEM.getKey(), mcId);
						tagMapBuilder.add(Pair.of(tagKey, componentId));
					}
				}
			}
		}
		this.itemToComponentIdMap = Map.copyOf(itemMap);
		this.componentIdToItemIdMap = Map.copyOf(reverseItemMap);
		this.tagToComponentIdMap = tagMapBuilder.build();
	}

	/**
	 * Converts a Minecraft ItemStack to a Forgero Component ID based on its type.
	 * This does NOT look at NBT data. It's a stateless mapping that checks direct item mappings first, then tag mappings.
	 *
	 * @param stack The item stack whose type to convert.
	 * @return An optional containing the component ID, or empty if no mapping exists.
	 */
	public Optional<OpenIdentifier> toComponentId(ItemStack stack) {
		if (stack == null || stack.isEmpty()) {
			return Optional.empty();
		}

		// Priority 1: Direct item mapping
		OpenIdentifier directMapping = itemToComponentIdMap.get(stack.getItem());
		if (directMapping != null) {
			return Optional.of(directMapping);
		}

		// Priority 2: Tag mapping
		for (Pair<TagKey<Item>, OpenIdentifier> pair : tagToComponentIdMap) {
			if (stack.isIn(pair.getFirst())) {
				return Optional.of(pair.getSecond());
			}
		}

		return Optional.empty();
	}

	/**
	 * Converts a Forgero Component ID to a Minecraft Item ID.
	 *
	 * @param componentId The component ID to convert.
	 * @return An optional containing the item ID, or empty if no direct mapping exists.
	 */
	public Optional<Identifier> toItemId(OpenIdentifier componentId) {
		return Optional.ofNullable(componentIdToItemIdMap.get(componentId));
	}

	/**
	 * Converts a Forgero Component ID to a Minecraft Item.
	 *
	 * @param componentId The component ID to convert.
	 * @return An optional containing the item, or empty if no mapping exists.
	 */
	public Optional<Item> toItem(OpenIdentifier componentId) {
		return toItemId(componentId).map(Registries.ITEM::get).filter(item -> item != Items.AIR);
	}
}
