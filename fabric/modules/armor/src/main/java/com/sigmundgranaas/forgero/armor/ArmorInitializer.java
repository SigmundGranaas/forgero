package com.sigmundgranaas.forgero.armor;

import com.sigmundgranaas.forgero.armor.item.ForgeroArmorItem;
import com.sigmundgranaas.forgero.armor.item.ForgeroArmorMaterial;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.property.api.Resolver;
import com.sigmundgranaas.forgero.core.property.engine.ResolverEngine;
import com.sigmundgranaas.forgero.data.loading.api.data.host.CreateData;
import com.sigmundgranaas.forgero.data.loading.api.data.host.HostData;
import com.sigmundgranaas.forgero.data.pipeline.api.ForgeroDataBundle;
import com.sigmundgranaas.forgero.data.pipeline.api.ForgeroDataInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class ArmorInitializer implements ModInitializer {
	public static final String MOD_NAMESPACE = "forgero";
	public static final Logger LOGGER = LoggerFactory.getLogger(ArmorInitializer.class);
	public static final String ARMOR_ITEM_CLASS = "forgero:armor_item";

	private static final Map<String, RegistryKey<ItemGroup>> ITEM_GROUP_KEY_MAP = new HashMap<>();

	private record PendingItemGroupRegistration(Item item, @Nullable String groupId) {
	}

	@Override
	public void onInitialize() {
		long totalStartTime = System.currentTimeMillis();
		LOGGER.info("Starting Forgero Armor module initialization.");

		initializeItemGroupKeys();

		// Load the data bundle, which contains component and host item information.
		// This is needed on both server (for properties) and client (for visuals).
		ForgeroDataInitializer initializer = new ForgeroDataInitializer(MOD_NAMESPACE);
		ForgeroDataBundle bundle = initializer.getDataBundle();
		LOGGER.info("Data bundle loaded with {} component entries.", bundle.componentRegistry().all().size());

		// Register all armor items defined in the data files.
		List<PendingItemGroupRegistration> pendingRegistrations = registerArmorItems(bundle);
		addItemsToGroups(pendingRegistrations);

		long totalEndTime = System.currentTimeMillis();
		LOGGER.info("Forgero Armor module initialization complete. Took {}ms.", totalEndTime - totalStartTime);
	}

	private void initializeItemGroupKeys() {
		ITEM_GROUP_KEY_MAP.put("forgero:combat", ItemGroups.COMBAT);
		ITEM_GROUP_KEY_MAP.put("minecraft:combat", ItemGroups.COMBAT);
		ITEM_GROUP_KEY_MAP.put("forgero:tools", ItemGroups.TOOLS);
		ITEM_GROUP_KEY_MAP.put("minecraft:tools_and_utilities", ItemGroups.TOOLS);
	}

	private List<PendingItemGroupRegistration> registerArmorItems(ForgeroDataBundle bundle) {
		long startTime = System.currentTimeMillis();
		var hostItemMap = bundle.hostItemMap();
		LOGGER.info("Starting registration of Forgero armor items. Found {} host entries to process.", hostItemMap.size());

		var componentRegistry = bundle.componentRegistry();
		Resolver resolver = new ResolverEngine();
		List<PendingItemGroupRegistration> pendingRegistrations = new ArrayList<>();
		AtomicInteger successCount = new AtomicInteger(0);

		hostItemMap.forEach((componentId, hostData) -> {
			if (shouldCreateArmorItem(hostData)) {
				CreateData createData = hostData.create();
				var componentOpt = componentRegistry.find(componentId);

				if (componentOpt.isEmpty()) {
					LOGGER.warn("Failed to create armor item for component '{}'. Component not found in registry.", componentId);
					return;
				}
				var component = componentOpt.get();
				var armorType = determineArmorType(component);

				if (armorType == null) {
					LOGGER.warn("Failed to create armor item for component '{}'. Could not determine armor type from tags.", componentId);
					return;
				}

				ForgeroArmorMaterial material = new ForgeroArmorMaterial(component, resolver);
				ForgeroArmorItem item = new ForgeroArmorItem(material, armorType, new Item.Settings(), component);

				Identifier minecraftId = new Identifier(createData.id().namespace(), createData.id().path());
				Registry.register(Registries.ITEM, minecraftId, item);
				LOGGER.debug("Successfully registered armor item: {}", minecraftId);

				pendingRegistrations.add(new PendingItemGroupRegistration(item, createData.item_group()));
				successCount.getAndIncrement();
			} else {
				logSkippedItem(componentId, hostData);
			}
		});

		long endTime = System.currentTimeMillis();
		LOGGER.info("Finished armor item registration. Registered {} items in {}ms.", successCount.get(), endTime - startTime);
		return pendingRegistrations;
	}

	private void logSkippedItem(OpenIdentifier componentId, HostData hostData) {
		if (hostData.create() == null) {
			LOGGER.trace("Skipping component [{}]: HostData has no 'create' block.", componentId);
		} else if (!ARMOR_ITEM_CLASS.equals(hostData.create().itemClass())) {
			LOGGER.trace("Skipping component [{}]: Item class '{}' is not an armor item ('{}').", componentId, hostData.create().itemClass(), ARMOR_ITEM_CLASS);
		}
	}

	private void addItemsToGroups(List<PendingItemGroupRegistration> pendingRegistrations) {
		if (pendingRegistrations.isEmpty()) {
			LOGGER.info("No new armor items to add to creative tabs.");
			return;
		}

		LOGGER.info("Adding {} armor items to their respective creative tabs...", pendingRegistrations.size());
		long startTime = System.currentTimeMillis();

		pendingRegistrations.forEach(reg -> {
			RegistryKey<ItemGroup> groupKey = resolveItemGroupKey(reg.groupId());
			if (groupKey != null) {
				ItemGroupEvents.modifyEntriesEvent(groupKey).register(entries -> {
					entries.add(reg.item());
					LOGGER.trace("Added item {} to item group {}", Registries.ITEM.getId(reg.item()), groupKey.getValue());
				});
			}
		});

		long endTime = System.currentTimeMillis();
		LOGGER.info("Finished queuing items for creative tabs in {}ms.", endTime - startTime);
	}

	@Nullable
	private RegistryKey<ItemGroup> resolveItemGroupKey(@Nullable String groupId) {
		String effectiveGroupId = (groupId == null || groupId.isBlank()) ? "minecraft:combat" : groupId;
		RegistryKey<ItemGroup> key = ITEM_GROUP_KEY_MAP.get(effectiveGroupId);
		if (key == null) {
			LOGGER.warn("Could not find an ItemGroup for ID: '{}'. The item will not be added to a creative tab.", effectiveGroupId);
		}
		return key;
	}

	private boolean shouldCreateArmorItem(HostData hostData) {
		return hostData.create() != null && ARMOR_ITEM_CLASS.equals(hostData.create().itemClass());
	}

	private ArmorItem.Type determineArmorType(Component component) {
		var tags = component.getTags().stream().map(OpenIdentifier::name).collect(Collectors.toSet());
		if (tags.contains("helmet")) return ArmorItem.Type.HELMET;
		if (tags.contains("chestplate")) return ArmorItem.Type.CHESTPLATE;
		if (tags.contains("leggings")) return ArmorItem.Type.LEGGINGS;
		if (tags.contains("boots")) return ArmorItem.Type.BOOTS;
		return null;
	}
}
