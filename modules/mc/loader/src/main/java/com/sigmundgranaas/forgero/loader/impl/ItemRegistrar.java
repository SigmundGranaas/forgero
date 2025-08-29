package com.sigmundgranaas.forgero.loader.impl;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.property.api.Resolver;
import com.sigmundgranaas.forgero.core.registry.ComponentRegistry;
import com.sigmundgranaas.forgero.data.loading.api.data.host.CreateData;
import com.sigmundgranaas.forgero.data.loading.api.data.host.HostData;
import com.sigmundgranaas.forgero.loader.api.ItemCreator;
import com.sigmundgranaas.forgero.loader.api.ItemRegistrationCallback;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Handles the registration of all Forgero items based on data and plugin-provided creators.
 */
public class ItemRegistrar {
	private static final Map<String, RegistryKey<ItemGroup>> ITEM_GROUP_KEY_MAP = new HashMap<>();

	private final ComponentRegistry componentRegistry;
	private final Resolver resolver;
	private final Logger logger;
	private final List<ItemRegistrationCallback> registrationCallbacks = new ArrayList<>();

	static {
		initializeItemGroupKeys();
	}

	private static void initializeItemGroupKeys() {
		ITEM_GROUP_KEY_MAP.put("forgero:combat", ItemGroups.COMBAT);
		ITEM_GROUP_KEY_MAP.put("minecraft:combat", ItemGroups.COMBAT);
		ITEM_GROUP_KEY_MAP.put("forgero:tools", ItemGroups.TOOLS);
		ITEM_GROUP_KEY_MAP.put("minecraft:tools_and_utilities", ItemGroups.TOOLS);
		ITEM_GROUP_KEY_MAP.put("forgero:ingredients", ItemGroups.INGREDIENTS);
		ITEM_GROUP_KEY_MAP.put("minecraft:ingredients", ItemGroups.INGREDIENTS);
	}

	public record PendingItemGroupRegistration(Item item, @Nullable String groupId) {}
	public record RegisteredItem(Identifier id, Item item, Component component, CreateData createData) {}

	public ItemRegistrar(ComponentRegistry componentRegistry, Resolver resolver, Logger logger) {
		this.componentRegistry = componentRegistry;
		this.resolver = resolver;
		this.logger = logger;
	}

	/**
	 * Adds a callback that will be notified for each item registered.
	 * This allows plugins to hook into the registration process.
	 */
	public void addRegistrationCallback(ItemRegistrationCallback callback) {
		this.registrationCallbacks.add(callback);
	}

	/**
	 * Registers all items based on the host item map and available creators.
	 * @return List of all successfully registered items
	 */
	public List<RegisteredItem> registerItems(Map<OpenIdentifier, HostData> hostItemMap, Map<String, ItemCreator> creators) {
		long startTime = System.currentTimeMillis();
		logger.info("Starting item registration. Found {} host entries to process with {} item creators.",
				hostItemMap.size(), creators.size());

		List<PendingItemGroupRegistration> pendingGroupRegistrations = new ArrayList<>();
		List<RegisteredItem> registeredItems = new ArrayList<>();
		AtomicInteger successCount = new AtomicInteger(0);
		AtomicInteger skipCount = new AtomicInteger(0);
		AtomicInteger failCount = new AtomicInteger(0);

		hostItemMap.forEach((componentId, hostData) -> {
			CreateData createData = hostData.create();

			if (!shouldCreateItem(createData, creators)) {
				logSkippedItem(componentId, hostData, creators);
				skipCount.incrementAndGet();
				return;
			}

			Optional<Component> componentOpt = componentRegistry.get(componentId);
			if (componentOpt.isEmpty()) {
				logger.warn("Failed to create item for component '{}'. Component not found in registry.", componentId);
				failCount.incrementAndGet();
				return;
			}

			Component component = componentOpt.get();
			ItemCreator creator = creators.get(createData.itemClass());

			try {
				// Create the item
				Item item = creator.create(component, createData, resolver);
				Identifier minecraftId = new Identifier(createData.id().namespace(), createData.id().path());

				// Allow callbacks to modify the item before registration
				for (ItemRegistrationCallback callback : registrationCallbacks) {
					item = callback.onItemPreRegister(minecraftId, item, component, createData);
				}

				// Register the item
				Registry.register(Registries.ITEM, minecraftId, item);
				logger.debug("Successfully registered item: {}", minecraftId);

				// Track for group registration
				pendingGroupRegistrations.add(new PendingItemGroupRegistration(item, createData.itemGroup()));

				// Track registered item
				RegisteredItem registeredItem = new RegisteredItem(minecraftId, item, component, createData);
				registeredItems.add(registeredItem);

				// Notify callbacks after registration
				for (ItemRegistrationCallback callback : registrationCallbacks) {
					callback.onItemPostRegister(minecraftId, item, component, createData);
				}

				successCount.incrementAndGet();

			} catch (Exception e) {
				logger.error("Failed to create item for component '{}' using creator for class '{}'",
						componentId, createData.itemClass(), e);
				failCount.incrementAndGet();
			}
		});

		long endTime = System.currentTimeMillis();
		logger.info("Finished item registration. Registered {} items, skipped {}, failed {} in {}ms.",
				successCount.get(), skipCount.get(), failCount.get(), endTime - startTime);

		// Add items to creative tabs
		addItemsToGroups(pendingGroupRegistrations);

		return registeredItems;
	}

	private boolean shouldCreateItem(@Nullable CreateData createData, Map<String, ItemCreator> creators) {
		return createData != null && creators.containsKey(createData.itemClass());
	}

	private void logSkippedItem(OpenIdentifier componentId, HostData hostData, Map<String, ItemCreator> creators) {
		if (hostData.create() == null) {
			logger.trace("Skipping component [{}]: HostData has no 'create' block.", componentId);
		} else if (!creators.containsKey(hostData.create().itemClass())) {
			logger.trace("Skipping component [{}]: No ItemCreator registered for class '{}'.",
					componentId, hostData.create().itemClass());
		}
	}

	private void addItemsToGroups(List<PendingItemGroupRegistration> pendingRegistrations) {
		if (pendingRegistrations.isEmpty()) {
			logger.debug("No new items to add to creative tabs.");
			return;
		}

		logger.info("Adding {} items to their respective creative tabs...", pendingRegistrations.size());
		long startTime = System.currentTimeMillis();

		// Group items by their target ItemGroup to reduce event registrations
		Map<RegistryKey<ItemGroup>, List<Item>> itemsByGroup = new HashMap<>();

		for (PendingItemGroupRegistration reg : pendingRegistrations) {
			RegistryKey<ItemGroup> groupKey = resolveItemGroupKey(reg.groupId());
			if (groupKey != null) {
				itemsByGroup.computeIfAbsent(groupKey, k -> new ArrayList<>()).add(reg.item());
			}
		}

		// Register all items for each group in a single event handler
		itemsByGroup.forEach((groupKey, items) -> {
			ItemGroupEvents.modifyEntriesEvent(groupKey).register(entries -> {
				for (Item item : items) {
					entries.add(item);
					logger.trace("Added item {} to item group {}",
							Registries.ITEM.getId(item), groupKey.getValue());
				}
			});
			logger.debug("Queued {} items for creative tab {}", items.size(), groupKey.getValue());
		});

		long endTime = System.currentTimeMillis();
		logger.info("Finished queuing items for creative tabs in {}ms.", endTime - startTime);
	}

	@Nullable
	private RegistryKey<ItemGroup> resolveItemGroupKey(@Nullable String groupId) {
		String effectiveGroupId = (groupId == null || groupId.isBlank())
				? "minecraft:tools_and_utilities"
				: groupId;

		RegistryKey<ItemGroup> key = ITEM_GROUP_KEY_MAP.get(effectiveGroupId);
		if (key == null) {
			logger.warn("Could not find an ItemGroup for ID: '{}'. The item will not be added to a creative tab.",
					effectiveGroupId);
		}
		return key;
	}
}
