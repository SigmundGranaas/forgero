package com.sigmundgranaas.forgero.render;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.utility.resource.loader.api.ResourceFilter;
import com.sigmundgranaas.forgero.utility.resource.loader.api.ResourcePath;
import com.sigmundgranaas.forgero.utility.resource.loader.api.ResourceProvider;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * A ResourceProvider that wraps Minecraft's ResourceManager.
 * <p>
 * This provider enables Forgero to access resources through Minecraft's resource pack system,
 * allowing resource packs to override mod resources using standard Minecraft conventions.
 * <p>
 * Supports the unified resource loading architecture with configurable filtering
 * and priority-based composition.
 */
public class MinecraftResourceProvider implements ResourceProvider {
	private static final Logger LOGGER = LoggerFactory.getLogger(MinecraftResourceProvider.class);

	private final ResourceManager resourceManager;
	private final String pathPrefix;
	private final int providerPriority;

	/**
	 * Creates a MinecraftResourceProvider with full configuration.
	 *
	 * @param resourceManager The Minecraft ResourceManager to wrap
	 * @param pathPrefix      A prefix to prepend to all paths (e.g., "textures" for assets)
	 * @param priority        The provider priority for composite ordering
	 */
	public MinecraftResourceProvider(ResourceManager resourceManager, String pathPrefix, int priority) {
		this.resourceManager = resourceManager;
		this.pathPrefix = pathPrefix == null ? "" : pathPrefix.replaceAll("^/+|/+$", "");
		this.providerPriority = priority;
	}

	/**
	 * Creates a MinecraftResourceProvider with no path prefix and default priority.
	 *
	 * @param resourceManager The Minecraft ResourceManager to wrap
	 */
	public MinecraftResourceProvider(ResourceManager resourceManager) {
		this(resourceManager, "", 100);
	}

	/**
	 * Creates a MinecraftResourceProvider with path prefix and default priority.
	 *
	 * @param resourceManager The Minecraft ResourceManager to wrap
	 * @param pathPrefix      A prefix to prepend to all paths
	 */
	public MinecraftResourceProvider(ResourceManager resourceManager, String pathPrefix) {
		this(resourceManager, pathPrefix, 100);
	}

	@Override
	public Set<String> getNamespaces() {
		return resourceManager.getAllNamespaces();
	}

	@Override
	public Stream<ResourcePath> list(ResourcePath path, boolean recursive, ResourceFilter filter) {
		String searchPath = pathPrefix.isEmpty() ? path.directory() : pathPrefix + "/" + path.directory();

		return resourceManager.findResources(searchPath, id -> {
					// Convert to ResourcePath and check filter
					ResourcePath rp = toResourcePath(id);
					return rp.namespace().equals(path.namespace()) && filter.test(rp);
				})
				.keySet()
				.stream()
				.filter(id -> id.getNamespace().equals(path.namespace()))
				.map(this::toResourcePath);
	}

	@Override
	public Stream<OpenIdentifier> list(OpenIdentifier path, boolean recursive) {
		return list(ResourcePath.directory(path.namespace(), path.path()), recursive, ResourceFilter.JSON)
				.map(ResourcePath::toIdentifier);
	}

	@Override
	public Optional<InputStream> read(ResourcePath path) {
		String fullPath = pathPrefix.isEmpty() ? path.fullPath() : pathPrefix + "/" + path.fullPath();
		Identifier mcIdentifier = new Identifier(path.namespace(), fullPath);

		return resourceManager.getResource(mcIdentifier).flatMap(resource -> {
			try {
				return Optional.of(resource.getInputStream());
			} catch (IOException e) {
				LOGGER.warn("Failed to read resource stream for identifier: {}", mcIdentifier, e);
				return Optional.empty();
			}
		});
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
		return "MinecraftResourceManager" + (pathPrefix.isEmpty() ? "" : "[" + pathPrefix + "]");
	}

	/**
	 * Converts a Minecraft Identifier to a ResourcePath.
	 */
	private ResourcePath toResourcePath(Identifier id) {
		String path = id.getPath();
		// Remove path prefix if present
		if (!pathPrefix.isEmpty() && path.startsWith(pathPrefix + "/")) {
			path = path.substring(pathPrefix.length() + 1);
		}
		return ResourcePath.fromIdentifier(new OpenIdentifier(id.getNamespace(), path));
	}
}
