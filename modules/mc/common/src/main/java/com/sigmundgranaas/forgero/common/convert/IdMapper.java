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

import net.minecraft.registry.entry.RegistryEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.StreamSupport;

/**
 * Handles stateless, bidirectional mapping between Minecraft Identifiers (for Items and Tags)
 * and Forgero OpenIdentifiers (for Components).
 * This class is initialized with data mappings and provides fast lookups.
 * Tag-based reverse mappings are resolved lazily to ensure tags are loaded.
 */
public class IdMapper {
	private static final Logger LOGGER = LoggerFactory.getLogger(IdMapper.class);

	private final Map<Item, OpenIdentifier> itemToComponentIdMap;
	private final Map<OpenIdentifier, Identifier> componentIdToItemIdMap;
	private final ImmutableList<Pair<TagKey<Item>, OpenIdentifier>> tagToComponentIdMap;
	// Lazy cache for tag-based reverse mappings (component -> item via tag)
	private final Map<OpenIdentifier, Identifier> lazyTagReverseMap = new ConcurrentHashMap<>();
	private volatile boolean tagResolutionAttempted = false;

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
						// Tag-based reverse mappings are resolved lazily in toItemId()
						// to ensure datapack tags are loaded before resolution
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
	 * Tag-based mappings are resolved lazily on first access to ensure tags are loaded.
	 *
	 * @param componentId The component ID to convert.
	 * @return An optional containing the item ID, or empty if no mapping exists.
	 */
	public Optional<Identifier> toItemId(OpenIdentifier componentId) {
		// First check the direct/item-based mappings
		Identifier directMapping = componentIdToItemIdMap.get(componentId);
		if (directMapping != null) {
			return Optional.of(directMapping);
		}

		// Lazily resolve tag-based mappings if not already attempted
		ensureTagResolution();

		// Check the lazy tag cache
		return Optional.ofNullable(lazyTagReverseMap.get(componentId));
	}

	/**
	 * Ensures tag-based reverse mappings have been resolved.
	 * This is done lazily to ensure datapack tags are loaded before resolution.
	 */
	private void ensureTagResolution() {
		if (tagResolutionAttempted) {
			return;
		}
		synchronized (this) {
			if (tagResolutionAttempted) {
				return;
			}
			LOGGER.debug("Lazily resolving tag-based reverse mappings...");
			for (Pair<TagKey<Item>, OpenIdentifier> pair : tagToComponentIdMap) {
				// Skip if already has a direct mapping
				if (componentIdToItemIdMap.containsKey(pair.getSecond())) {
					continue;
				}
				// Skip if already resolved in lazy cache
				if (lazyTagReverseMap.containsKey(pair.getSecond())) {
					continue;
				}
				resolveFirstItemFromTag(pair.getFirst(), pair.getSecond()).ifPresent(item -> {
					Identifier itemId = Registries.ITEM.getId(item);
					lazyTagReverseMap.putIfAbsent(pair.getSecond(), itemId);
				});
			}
			tagResolutionAttempted = true;
		}
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

	/**
	 * Resolves a tag to its first item, prioritizing items from the 'minecraft' namespace.
	 * This method is used during initialization to establish a default reverse mapping
	 * for tag-based component host definitions.
	 *
	 * @param tagKey      The tag to resolve.
	 * @param componentId The component ID this tag is associated with (for logging).
	 * @return The first item in the tag, preferring minecraft namespace items, or empty if tag is empty/missing.
	 */
	private Optional<Item> resolveFirstItemFromTag(TagKey<Item> tagKey, OpenIdentifier componentId) {
		List<Item> tagItems;
		try {
			tagItems = StreamSupport.stream(Registries.ITEM.iterateEntries(tagKey).spliterator(), false)
					.map(RegistryEntry::value)
					.filter(item -> item != Items.AIR)
					.toList();
		} catch (Exception e) {
			LOGGER.error("Failed to resolve tag '{}' for component '{}': {}",
					tagKey.id(), componentId, e.getMessage());
			return Optional.empty();
		}

		if (tagItems.isEmpty()) {
			LOGGER.warn("Tag '{}' is empty or not found for component '{}'. " +
					"Component-to-Item conversion will not be available.",
					tagKey.id(), componentId);
			return Optional.empty();
		}

		// Priority 1: Minecraft namespace items
		Optional<Item> minecraftItem = tagItems.stream()
				.filter(item -> Registries.ITEM.getId(item).getNamespace().equals("minecraft"))
				.findFirst();

		if (minecraftItem.isPresent()) {
			LOGGER.debug("Resolved tag '{}' to minecraft item '{}' for component '{}'",
					tagKey.id(), Registries.ITEM.getId(minecraftItem.get()), componentId);
			return minecraftItem;
		}

		// Priority 2: Registry order (first item)
		Item firstItem = tagItems.get(0);
		LOGGER.debug("No minecraft item in tag '{}', using first available '{}' for component '{}'",
				tagKey.id(), Registries.ITEM.getId(firstItem), componentId);
		return Optional.of(firstItem);
	}
}
