package com.sigmundgranaas.forgero.tools;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resource.DirectoryResourcePack;
import net.minecraft.resource.ResourcePackProfile;
import net.minecraft.resource.ResourcePackProvider;
import net.minecraft.resource.ResourcePackSource;
import net.minecraft.resource.ResourceType;
import net.minecraft.text.Text;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;

public class DevResourcePackProvider implements ResourcePackProvider {
	private static final String DEV_HOT_RELOAD_PROPERTY = "forgero.dev.hotReload";
	private static final String DEV_RESOURCE_PATHS_PROPERTY = "forgero.dev.resourcePaths";
	private static final ResourcePackSource FORGERO_DEV_SOURCE = ResourcePackSource.create(name -> Text.literal("Forgero Dev"), true);

	@Override
	public void register(Consumer<ResourcePackProfile> profileAdder) {
		String hotReloadFlag = System.getProperty(DEV_HOT_RELOAD_PROPERTY);
		if (!"true".equalsIgnoreCase(hotReloadFlag) || !FabricLoader.getInstance().isDevelopmentEnvironment()) {
			return;
		}

		String resourcePaths = System.getProperty(DEV_RESOURCE_PATHS_PROPERTY);
		if (resourcePaths == null || resourcePaths.isBlank()) {
			DevPlugin.LOGGER.warn("Forgero hot-reload is enabled, but no resource paths were provided via system property.");
			return;
		}

		DevPlugin.LOGGER.info("Forgero Dev Mode: Registering source directories for automatic hot-reloading.");

		int packCounter = 0;
		for (String pathString : resourcePaths.split(File.pathSeparator)) {
			Path resourcePath = Paths.get(pathString);
			if (Files.exists(resourcePath) && Files.isDirectory(resourcePath)) {
				String packName = "forgero_dev_" + packCounter++; // Use a machine-friendly name

				ResourcePackProfile.PackFactory packFactory = (name) -> new DirectoryResourcePack(name, resourcePath, true);

				ResourcePackProfile profile = ResourcePackProfile.create(
						packName,
						Text.literal("Forgero Dev Source (" + resourcePath.getParent().getFileName().toString() + ")"),
						true,
						packFactory,
						ResourceType.CLIENT_RESOURCES,
						ResourcePackProfile.InsertionPosition.TOP,
						FORGERO_DEV_SOURCE
				);

				if (profile != null) {
					profileAdder.accept(profile);
					DevPlugin.LOGGER.info("Registered source resource pack: {}", resourcePath);
				}
			}
		}
	}
}
