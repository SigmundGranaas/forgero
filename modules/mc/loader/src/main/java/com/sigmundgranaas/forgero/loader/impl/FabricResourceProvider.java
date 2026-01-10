package com.sigmundgranaas.forgero.loader.impl;

import static com.sigmundgranaas.forgero.utility.resource.loader.api.ResourceConstants.*;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.utility.resource.loader.api.ResourceFilter;
import com.sigmundgranaas.forgero.utility.resource.loader.api.ResourcePath;
import com.sigmundgranaas.forgero.utility.resource.loader.api.ResourceProvider;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A Fabric-specific ResourceProvider that discovers resources from all mods
 * marked with the "forgeroResource" custom value in their fabric.mod.json.
 * <p>
 * This provider uses Fabric's ModContainer API to properly access resources
 * from nested JARs and development classpath entries.
 * <p>
 * Supports the unified resource loading architecture with configurable filtering
 * and priority-based composition.
 */
public class FabricResourceProvider implements ResourceProvider {
	private static final Logger LOGGER = LoggerFactory.getLogger(FabricResourceProvider.class);

	private final String topLevelDirectory;
	private final List<ModContainer> forgeroResourceMods;
	private final int providerPriority;

	/**
	 * Creates a new FabricResourceProvider with specified priority.
	 *
	 * @param topLevelDirectory The root directory within mod resources (e.g., "data").
	 * @param priority          The provider priority for composite ordering.
	 */
	public FabricResourceProvider(String topLevelDirectory, int priority) {
		this.topLevelDirectory = topLevelDirectory.replaceAll("^/|/$", "");
		this.providerPriority = priority;
		this.forgeroResourceMods = discoverForgeroResourceMods();
		LOGGER.info("FabricResourceProvider initialized with {} Forgero resource mods: {}",
				forgeroResourceMods.size(),
				forgeroResourceMods.stream()
						.map(m -> m.getMetadata().getId())
						.collect(Collectors.joining(", ")));
	}

	/**
	 * Creates a new FabricResourceProvider with default priority.
	 *
	 * @param topLevelDirectory The root directory within mod resources (e.g., "data").
	 */
	public FabricResourceProvider(String topLevelDirectory) {
		this(topLevelDirectory, PRIORITY_MOD);
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
		if (metadata.getId().equals(NAMESPACE_MINECRAFT)) {
			return true;
		}
		// Check for forgeroResource custom value
		if (metadata.containsCustomValue(FABRIC_FORGERO_RESOURCE_KEY)) {
			try {
				return metadata.getCustomValue(FABRIC_FORGERO_RESOURCE_KEY).getAsBoolean();
			} catch (Exception e) {
				LOGGER.warn("Mod {} has invalid forgeroResource value", metadata.getId());
				return false;
			}
		}
		return false;
	}

	@Override
	public Set<String> getNamespaces() {
		return DEFAULT_NAMESPACES;
	}

	@Override
	public Stream<ResourcePath> list(ResourcePath path, boolean recursive, ResourceFilter filter) {
		return forgeroResourceMods.stream()
				.flatMap(mod -> listFromMod(mod, path, recursive, filter));
	}

	@Override
	public Stream<OpenIdentifier> list(OpenIdentifier path, boolean recursive) {
		return list(ResourcePath.directory(path.namespace(), path.path()), recursive, ResourceFilter.JSON)
				.map(ResourcePath::toIdentifier);
	}

	private Stream<ResourcePath> listFromMod(ModContainer mod, ResourcePath path, boolean recursive, ResourceFilter filter) {
		// Build the full path: topLevelDirectory/namespace/directory
		String fullPath = buildFullPath(path);

		Optional<Path> rootPath = mod.findPath(fullPath);
		if (rootPath.isEmpty()) {
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
						.map(filePath -> {
							// Get path relative to namespace root
							Path relativePath = namespaceBase.relativize(filePath);
							String relativePathString = relativePath.toString().replace('\\', '/');
							return ResourcePath.fromIdentifier(new OpenIdentifier(path.namespace(), relativePathString));
						})
						.filter(filter)
						.toList().stream(); // Collect to list to avoid stream closed issues
			}
		} catch (IOException e) {
			LOGGER.error("Error listing resources from mod {} at path {}",
					mod.getMetadata().getId(), fullPath, e);
			return Stream.empty();
		}
	}

	@Override
	public Optional<InputStream> read(ResourcePath path) {
		String fullPath = topLevelDirectory + "/" + path.namespace() + "/" + path.fullPath();

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

	@Override
	public Optional<InputStream> read(OpenIdentifier identifier) {
		return read(ResourcePath.fromIdentifier(identifier));
	}

	@Override
	public int priority() {
		return providerPriority;
	}

	@Override
	public String name() {
		return "FabricResourceProvider[" + topLevelDirectory + "]";
	}

	private String buildFullPath(ResourcePath path) {
		if (path.directory().isEmpty()) {
			return topLevelDirectory + "/" + path.namespace();
		}
		return topLevelDirectory + "/" + path.namespace() + "/" + path.directory();
	}
}
