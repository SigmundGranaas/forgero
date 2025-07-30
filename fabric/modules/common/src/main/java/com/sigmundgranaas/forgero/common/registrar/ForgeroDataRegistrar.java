package com.sigmundgranaas.forgero.common.registrar;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.property.api.Resolver;
import com.sigmundgranaas.forgero.core.property.engine.ResolverEngine;
import com.sigmundgranaas.forgero.data.loading.api.data.host.CreateData;
import com.sigmundgranaas.forgero.data.loading.api.data.host.HostData;
import com.sigmundgranaas.forgero.data.pipeline.api.ForgeroDataBundle;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A centralized registrar for processing Forgero data and registering items.
 * This class implements the common pipeline for turning data-driven components into
 * actual in-game items based on a set of provided ItemCreators.
 */
public class ForgeroDataRegistrar {
	private static final Map<String, RegistryKey<ItemGroup>> ITEM_GROUP_KEY_MAP = new HashMap<>();
	private final Logger logger;

	private record PendingItemGroupRegistration(Item item, @Nullable String groupId) {
	}

	public ForgeroDataRegistrar(Logger logger) {
		this.logger = logger;
		initializeItemGroupKeys();
	}

	private void initializeItemGroupKeys() {
		ITEM_GROUP_KEY_MAP.put("forgero:combat", ItemGroups.COMBAT);
		ITEM_GROUP_KEY_MAP.put("minecraft:combat", ItemGroups.COMBAT);
		ITEM_GROUP_KEY_MAP.put("forgero:tools", ItemGroups.TOOLS);
		ITEM_GROUP_KEY_MAP.put("minecraft:tools_and_utilities", ItemGroups.TOOLS);
	}

	/**
	 * Processes the host item data from a bundle and registers items using the provided creators.
	 *
	 * @param bundle   The data bundle containing all component and host item definitions.
	 * @param creators A map of item class names (e.g., "forgero:armor_item") to their corresponding ItemCreator logic.
	 */
	public void process(ForgeroDataBundle bundle, Map<String, ItemCreator> creators) {
		long startTime = System.currentTimeMillis();
		var hostItemMap = bundle.hostItemMap();
		logger.info("Starting registration process. Found {} host entries to process with {} item creators.", hostItemMap.size(), creators.size());

		var componentRegistry = bundle.componentRegistry();
		Resolver resolver = new ResolverEngine();
		List<PendingItemGroupRegistration> pendingRegistrations = new ArrayList<>();
		AtomicInteger successCount = new AtomicInteger(0);

		hostItemMap.forEach((componentId, hostData) -> {
			CreateData createData = hostData.create();
			if (shouldCreateItem(createData, creators)) {
				var componentOpt = componentRegistry.find(componentId);

				if (componentOpt.isEmpty()) {
					logger.warn("Failed to create item for component '{}'. Component not found in registry.", componentId);
					return;
				}

				ItemCreator creator = creators.get(createData.itemClass());
				try {
					Item item = creator.create(componentOpt.get(), createData, resolver);
					Identifier minecraftId = new Identifier(createData.id().namespace(), createData.id().path());
					Registry.register(Registries.ITEM, minecraftId, item);
					logger.debug("Successfully registered item: {}", minecraftId);

					pendingRegistrations.add(new PendingItemGroupRegistration(item, createData.item_group()));
					successCount.getAndIncrement();
				} catch (Exception e) {
					logger.error("Failed to create item for component '{}' using creator for class '{}'", componentId, createData.itemClass(), e);
				}
			} else {
				logSkippedItem(componentId, hostData, creators);
			}
		});

		long endTime = System.currentTimeMillis();
		logger.info("Finished item registration. Registered {} items in {}ms.", successCount.get(), endTime - startTime);

		addItemsToGroups(pendingRegistrations);
	}

	private boolean shouldCreateItem(@Nullable CreateData createData, Map<String, ItemCreator> creators) {
		return createData != null && creators.containsKey(createData.itemClass());
	}

	private void logSkippedItem(OpenIdentifier componentId, HostData hostData, Map<String, ItemCreator> creators) {
		if (hostData.create() == null) {
			logger.trace("Skipping component [{}]: HostData has no 'create' block.", componentId);
		} else if (!creators.containsKey(hostData.create().itemClass())) {
			logger.trace("Skipping component [{}]: No ItemCreator registered for class '{}'.", componentId, hostData.create().itemClass());
		}
	}

	private void addItemsToGroups(List<PendingItemGroupRegistration> pendingRegistrations) {
		if (pendingRegistrations.isEmpty()) {
			logger.info("No new items to add to creative tabs.");
			return;
		}

		logger.info("Adding {} items to their respective creative tabs...", pendingRegistrations.size());
		long startTime = System.currentTimeMillis();

		pendingRegistrations.forEach(reg -> {
			RegistryKey<ItemGroup> groupKey = resolveItemGroupKey(reg.groupId());
			if (groupKey != null) {
				ItemGroupEvents.modifyEntriesEvent(groupKey).register(entries -> {
					entries.add(reg.item());
					logger.trace("Added item {} to item group {}", Registries.ITEM.getId(reg.item()), groupKey.getValue());
				});
			}
		});

		long endTime = System.currentTimeMillis();
		logger.info("Finished queuing items for creative tabs in {}ms.", endTime - startTime);
	}

	@Nullable
	private RegistryKey<ItemGroup> resolveItemGroupKey(@Nullable String groupId) {
		String effectiveGroupId = (groupId == null || groupId.isBlank()) ? "minecraft:tools_and_utilities" : groupId;
		RegistryKey<ItemGroup> key = ITEM_GROUP_KEY_MAP.get(effectiveGroupId);
		if (key == null) {
			logger.warn("Could not find an ItemGroup for ID: '{}'. The item will not be added to a creative tab.", effectiveGroupId);
		}
		return key;
	}
}
