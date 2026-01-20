package com.sigmundgranaas.forgero.drp.impl.pack;

import com.sigmundgranaas.forgero.drp.api.DynamicResourcePack;
import com.sigmundgranaas.forgero.drp.impl.DRPApiImpl;
import net.minecraft.resource.ResourcePackProfile;
import net.minecraft.resource.ResourcePackProvider;
import net.minecraft.resource.ResourcePackSource;
import net.minecraft.resource.ResourceType;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

/**
 * Resource pack provider that injects DRP packs into Minecraft's resource system.
 * <p>
 * This provider is registered via the fabric-resource-loader-v0:resource_pack_provider entrypoint.
 */
public class DRPResourcePackProvider implements ResourcePackProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger("DRP-Provider");
	private static final ResourcePackSource DRP_SOURCE = ResourcePackSource.create(
			name -> Text.literal("DRP: " + name.getString()),
			true
	);

	@Override
	public void register(Consumer<ResourcePackProfile> profileAdder) {
		DRPApiImpl api = DRPApiImpl.getInstance();

		for (DynamicResourcePack pack : api.getRegisteredPacks()) {
			if (!(pack instanceof DynamicResourcePackImpl packImpl)) {
				LOGGER.warn("Skipping non-standard DynamicResourcePack: {}", pack.getId());
				continue;
			}

			String packName = "drp/" + pack.getId().toString().replace(":", "_");

			// Create pack profile for server data (tags, recipes, etc.)
			ResourcePackProfile.PackFactory packFactory = name -> new InMemoryResourcePack(packImpl);

			ResourcePackProfile dataProfile = ResourcePackProfile.create(
					packName,
					Text.literal("DRP: " + pack.getId().toString()),
					true, // alwaysEnabled
					packFactory,
					ResourceType.SERVER_DATA,
					ResourcePackProfile.InsertionPosition.TOP,
					DRP_SOURCE
			);

			if (dataProfile != null) {
				profileAdder.accept(dataProfile);
				LOGGER.debug("Registered DRP data pack: {}", pack.getId());
			}

			// Also create pack profile for client assets (models, textures, lang, etc.)
			ResourcePackProfile assetProfile = ResourcePackProfile.create(
					packName + "_assets",
					Text.literal("DRP Assets: " + pack.getId().toString()),
					true, // alwaysEnabled
					packFactory,
					ResourceType.CLIENT_RESOURCES,
					ResourcePackProfile.InsertionPosition.TOP,
					DRP_SOURCE
			);

			if (assetProfile != null) {
				profileAdder.accept(assetProfile);
				LOGGER.debug("Registered DRP asset pack: {}", pack.getId());
			}
		}

		int packCount = api.getRegisteredPacks().size();
		if (packCount > 0) {
			LOGGER.debug("Registered {} DRP pack(s)", packCount);
		}
	}
}
