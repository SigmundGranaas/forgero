package com.sigmundgranaas.forgero.dynamicresourcepack.event;

import com.sigmundgranaas.forgero.dynamicresourcepack.api.resource.DynamicResourcePack;

import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourcePackProfile;
import net.minecraft.resource.ResourcePackSource;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ResourcePackEvent {
	private final static List<PackInfo> packs = new ArrayList<>();

	public static void beforeVanilla(ResourcePack packs, ResourceType resourceType) {
		ResourcePackEvent.packs.add(new PackInfo(resourceType, packs, ResourcePackProfile.InsertionPosition.BOTTOM));
	}

	public static void registerDynamicPacks(Consumer<ResourcePackProfile> consumer, ResourceType type) {
		packs.stream()
				.filter(t -> t.type == type)
				.map(ResourcePackEvent::createDynamicProvider)
				.forEach(consumer);
	}
	public static ResourcePackProfile createDynamicProvider(PackInfo info) {
		ResourcePackProfile.Metadata metadata = new ResourcePackProfile.Metadata(Text.of("test"), 0, FeatureSet.empty());
		return ResourcePackProfile.of(info.pack.getName(), Text.of("Test"),true, (String name) -> info.pack(), metadata ,info.type, ResourcePackProfile.InsertionPosition.BOTTOM, true, ResourcePackSource.BUILTIN);
	}

	public record PackInfo(ResourceType type, ResourcePack pack,
						   ResourcePackProfile.InsertionPosition position) {
	}
}
