package com.sigmundgranaas.drp.api;


import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.sigmundgranaas.drp.impl.pack.DynamicClientPack;
import com.sigmundgranaas.drp.impl.pack.DynamicServerPack;
import com.sigmundgranaas.drp.utils.Utils;

import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourcePackProfile;
import net.minecraft.resource.ResourcePackSource;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.metadata.PackResourceMetadata;
import net.minecraft.text.Text;

public class DynamicPackManager {
	private static final Map<String, PackInfo> packMap = new HashMap<>();

	public static DynamicServerPack createDataPack(String name) {
		var pack = new DynamicServerPack(name);
		registerPack(name, pack, ResourceType.SERVER_DATA, ResourcePackProfile.InsertionPosition.BOTTOM);
		return pack;
	}

	private static void registerPack(String name, ResourcePack pack, ResourceType type, ResourcePackProfile.InsertionPosition position) {
		var info = new PackInfo(type, pack, position);
		if (packMap.containsKey(name)) {
			Utils.LOGGER.warn("Overriding previously registered pack with name: {}, you are very likely trying to register another pack with the same name", name);
		}
		packMap.put(name, info);
	}

	public static DynamicClientPack createAssetPack(String name) {
		var pack = new DynamicClientPack(name);
		registerPack(name, pack, ResourceType.CLIENT_RESOURCES, ResourcePackProfile.InsertionPosition.BOTTOM);
		return pack;
	}

	public static void registerDynamicPacks(Consumer<ResourcePackProfile> consumer, ResourceType type) {
		packMap.values().stream()
				.filter(info -> info.type == type)
				.map(DynamicPackManager::createDynamicProvider)
				.forEach(consumer);
	}

	public static ResourcePackProfile createDynamicProvider(PackInfo info) {
		ResourcePackProfile.Factory factory = (String name, Text displayName, boolean alwaysEnabled, Supplier<ResourcePack> packFactory, PackResourceMetadata metadata, ResourcePackProfile.InsertionPosition initialPosition, ResourcePackSource source) -> new ResourcePackProfile(name, displayName, alwaysEnabled, packFactory, metadata, info.type(), initialPosition, source);

		return ResourcePackProfile.of(info.pack.getName(), true, info::pack, factory, info.position(), ResourcePackSource.PACK_SOURCE_BUILTIN);
	}

	public record PackInfo(ResourceType type, ResourcePack pack,
	                       ResourcePackProfile.InsertionPosition position) {
	}
}
