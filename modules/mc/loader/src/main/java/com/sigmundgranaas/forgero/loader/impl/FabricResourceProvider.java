package com.sigmundgranaas.forgero.loader.impl;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.utility.resource.loader.api.ResourceProvider;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A Fabric-specific ResourceProvider that discovers resources from all mods
 * marked with the "forgeroResource" custom value in their fabric.mod.json.
 * <p>
 * This provider uses Fabric's ModContainer API to properly access resources
 * from nested JARs and development classpath entries.
 */
public class FabricResourceProvider implements ResourceProvider {
	private static final Logger LOGGER = LoggerFactory.getLogger(FabricResourceProvider.class);
	private static final String FORGERO_RESOURCE_KEY = "forgeroResource";

	private final String topLevelDirectory;
	private final List<ModContainer> forgeroResourceMods;

	/**
	 * Creates a new FabricResourceProvider.
	 *
	 * @param topLevelDirectory The root directory within mod resources (e.g., "data").
	 */
	public FabricResourceProvider(String topLevelDirectory) {
		this.topLevelDirectory = topLevelDirectory.replaceAll("^/|/$", "");
		this.forgeroResourceMods = discoverForgeroResourceMods();
		LOGGER.info("FabricResourceProvider initialized with {} Forgero resource mods: {}",
				forgeroResourceMods.size(),
				forgeroResourceMods.stream()
						.map(m -> m.getMetadata().getId())
						.collect(Collectors.joining(", ")));
	}

	/**
	 * Discovers all mods that have "forgeroResource": true in their fabric.mod.json,
	 * plus Minecraft itself for vanilla data loading.
	 */
	private List<ModContainer> discoverForgeroResourceMods() {
		return FabricLoader.getInstance().getAllMods().stream()
				.filter(this::isForgeroResourceMod)
				.toList();
	}

	private boolean isForgeroResourceMod(ModContainer container) {
		var metadata = container.getMetadata();
		// Include Minecraft for vanilla data
		if (metadata.getId().equals("minecraft")) {
			return true;
		}
		// Check for forgeroResource custom value
		if (metadata.containsCustomValue(FORGERO_RESOURCE_KEY)) {
			try {
				return metadata.getCustomValue(FORGERO_RESOURCE_KEY).getAsBoolean();
			} catch (Exception e) {
				LOGGER.warn("Mod {} has invalid forgeroResource value", metadata.getId());
				return false;
			}
		}
		return false;
	}

	@Override
	public Set<String> getNamespaces() {
		return Set.of("forgero", "minecraft");
	}

	@Override
	public Stream<OpenIdentifier> list(OpenIdentifier path, boolean recursive) {
		return forgeroResourceMods.stream()
				.flatMap(mod -> listFromMod(mod, path, recursive));
	}

	private Stream<OpenIdentifier> listFromMod(ModContainer mod, OpenIdentifier path, boolean recursive) {
		// Build the full path: topLevelDirectory/namespace/path
		String fullPath = buildFullPath(path);

		Optional<Path> rootPath = mod.findPath(fullPath);
		if (rootPath.isEmpty()) {
			LOGGER.trace("Path {} not found in mod {}", fullPath, mod.getMetadata().getId());
			return Stream.empty();
		}

		Path startPath = rootPath.get();
		if (!Files.exists(startPath) || !Files.isDirectory(startPath)) {
			return Stream.empty();
		}

		try {
			int maxDepth = recursive ? Integer.MAX_VALUE : 1;
			// We need to capture the namespace root to calculate relative paths
			String namespaceRoot = topLevelDirectory + "/" + path.namespace();
			Optional<Path> namespaceRootPath = mod.findPath(namespaceRoot);

			if (namespaceRootPath.isEmpty()) {
				return Stream.empty();
			}

			Path namespaceBase = namespaceRootPath.get();

			try (Stream<Path> walk = Files.walk(startPath, maxDepth)) {
				return walk
						.filter(Files::isRegularFile)
						.filter(p -> p.toString().endsWith(".json"))
						.map(filePath -> {
							// Get path relative to namespace root
							Path relativePath = namespaceBase.relativize(filePath);
							String relativePathString = relativePath.toString().replace('\\', '/');
							return new OpenIdentifier(path.namespace(), relativePathString);
						})
						.toList().stream(); // Collect to list to avoid stream closed issues
			}
		} catch (IOException e) {
			LOGGER.error("Error listing resources from mod {} at path {}",
					mod.getMetadata().getId(), fullPath, e);
			return Stream.empty();
		}
	}

	@Override
	public Optional<InputStream> read(OpenIdentifier identifier) {
		String fullPath = topLevelDirectory + "/" + identifier.namespace() + "/" + identifier.path();

		for (ModContainer mod : forgeroResourceMods) {
			Optional<Path> resourcePath = mod.findPath(fullPath);
			if (resourcePath.isPresent() && Files.exists(resourcePath.get())) {
				try {
					return Optional.of(Files.newInputStream(resourcePath.get()));
				} catch (IOException e) {
					LOGGER.trace("Could not read {} from mod {}", fullPath, mod.getMetadata().getId());
				}
			}
		}

		LOGGER.trace("Resource not found in any Forgero resource mod: {}", fullPath);
		return Optional.empty();
	}

	private String buildFullPath(OpenIdentifier path) {
		if (path.path().isEmpty()) {
			return topLevelDirectory + "/" + path.namespace();
		}
		return topLevelDirectory + "/" + path.namespace() + "/" + path.path();
	}
}
