package com.sigmundgranaas.forgero.utility.resource.loader.implementation;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.utility.resource.loader.api.ResourceFilter;
import com.sigmundgranaas.forgero.utility.resource.loader.api.ResourcePath;
import com.sigmundgranaas.forgero.utility.resource.loader.api.ResourceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * A {@link ResourceProvider} that allows defining resources programmatically rather than from files.
 * <p>
 * ProgrammaticResourceProvider is useful for:
 * <ul>
 *   <li>Plugin-provided resources without requiring file packaging</li>
 *   <li>Dynamically generated content</li>
 *   <li>Test fixtures and mocking</li>
 *   <li>Default/fallback resources</li>
 *   <li>Runtime patching and overrides</li>
 * </ul>
 *
 * <h2>Resource Types</h2>
 * <p>
 * Resources can be registered as:
 * <ul>
 *   <li><b>Static</b> - Fixed content stored in memory</li>
 *   <li><b>Dynamic</b> - Content generated on-demand via a supplier</li>
 * </ul>
 *
 * <h2>Thread Safety</h2>
 * <p>
 * This class is thread-safe. Resources can be registered and unregistered from any thread.
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * ProgrammaticResourceProvider provider = new ProgrammaticResourceProvider("my-plugin", 100);
 *
 * // Register a static JSON resource
 * provider.registerJson("forgero", "materials/custom_material", """
 *     {
 *         "type": "forgero:material",
 *         "name": "custom_material",
 *         "properties": { }
 *     }
 *     """);
 *
 * // Register a dynamic resource
 * provider.registerDynamic(
 *     ResourcePath.file("forgero", "generated", "stats", "json"),
 *     () -> generateStatsJson().getBytes(StandardCharsets.UTF_8)
 * );
 *
 * // Use in a composite
 * CompositeResourceProvider composite = CompositeResourceProvider.builder()
 *     .add(provider)
 *     .add(new FabricResourceProvider("data"))
 *     .build();
 * }</pre>
 */
public class ProgrammaticResourceProvider implements ResourceProvider {
	private static final Logger LOGGER = LoggerFactory.getLogger(ProgrammaticResourceProvider.class);

	private final String providerName;
	private final int providerPriority;
	private final Set<String> namespaces = ConcurrentHashMap.newKeySet();

	// Thread-safe storage for resources
	private final Map<String, byte[]> staticResources = new ConcurrentHashMap<>();
	private final Map<String, Supplier<byte[]>> dynamicResources = new ConcurrentHashMap<>();

	/**
	 * Creates a new ProgrammaticResourceProvider with the specified name and priority.
	 *
	 * @param name     A descriptive name for this provider (used in logging)
	 * @param priority The priority for ordering (higher = checked first)
	 */
	public ProgrammaticResourceProvider(String name, int priority) {
		this.providerName = Objects.requireNonNull(name, "name cannot be null");
		this.providerPriority = priority;
	}

	/**
	 * Creates a new ProgrammaticResourceProvider with high default priority.
	 * Use this for test fixtures or overrides that should take precedence.
	 *
	 * @param name A descriptive name for this provider
	 */
	public ProgrammaticResourceProvider(String name) {
		this(name, 100);
	}

	// ========== Registration Methods ==========

	/**
	 * Registers a static resource with fixed byte content.
	 *
	 * @param path    The resource path
	 * @param content The content bytes
	 * @return This provider (for chaining)
	 */
	public ProgrammaticResourceProvider register(ResourcePath path, byte[] content) {
		Objects.requireNonNull(path, "path cannot be null");
		Objects.requireNonNull(content, "content cannot be null");

		namespaces.add(path.namespace());
		staticResources.put(path.toString(), content.clone());
		LOGGER.debug("Registered static resource: {}", path);
		return this;
	}

	/**
	 * Registers a static resource with string content (UTF-8 encoded).
	 *
	 * @param path    The resource path
	 * @param content The content string
	 * @return This provider (for chaining)
	 */
	public ProgrammaticResourceProvider register(ResourcePath path, String content) {
		return register(path, content.getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * Registers a JSON resource with convenient path specification.
	 * The path should not include the .json extension.
	 *
	 * @param namespace   The namespace (e.g., "forgero")
	 * @param path        The path within the namespace (e.g., "materials/iron")
	 * @param jsonContent The JSON content string
	 * @return This provider (for chaining)
	 */
	public ProgrammaticResourceProvider registerJson(String namespace, String path, String jsonContent) {
		// Parse path to separate directory and filename
		int lastSlash = path.lastIndexOf('/');
		String directory = lastSlash == -1 ? "" : path.substring(0, lastSlash);
		String fileName = lastSlash == -1 ? path : path.substring(lastSlash + 1);

		ResourcePath resourcePath = ResourcePath.file(namespace, directory, fileName, "json");
		return register(resourcePath, jsonContent);
	}

	/**
	 * Registers a dynamic resource with lazy content generation.
	 * The supplier is called each time the resource is read, allowing for
	 * content that changes over time.
	 *
	 * @param path            The resource path
	 * @param contentSupplier A supplier that generates the content bytes
	 * @return This provider (for chaining)
	 */
	public ProgrammaticResourceProvider registerDynamic(ResourcePath path, Supplier<byte[]> contentSupplier) {
		Objects.requireNonNull(path, "path cannot be null");
		Objects.requireNonNull(contentSupplier, "contentSupplier cannot be null");

		namespaces.add(path.namespace());
		dynamicResources.put(path.toString(), contentSupplier);
		LOGGER.debug("Registered dynamic resource: {}", path);
		return this;
	}

	/**
	 * Registers a dynamic resource with a string supplier.
	 *
	 * @param path            The resource path
	 * @param contentSupplier A supplier that generates the content string
	 * @return This provider (for chaining)
	 */
	public ProgrammaticResourceProvider registerDynamicString(ResourcePath path, Supplier<String> contentSupplier) {
		return registerDynamic(path, () -> contentSupplier.get().getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * Unregisters a resource.
	 *
	 * @param path The path of the resource to remove
	 * @return This provider (for chaining)
	 */
	public ProgrammaticResourceProvider unregister(ResourcePath path) {
		Objects.requireNonNull(path, "path cannot be null");
		String key = path.toString();
		staticResources.remove(key);
		dynamicResources.remove(key);
		LOGGER.debug("Unregistered resource: {}", path);
		return this;
	}

	/**
	 * Clears all registered resources.
	 *
	 * @return This provider (for chaining)
	 */
	public ProgrammaticResourceProvider clear() {
		staticResources.clear();
		dynamicResources.clear();
		namespaces.clear();
		LOGGER.debug("Cleared all resources from provider: {}", providerName);
		return this;
	}

	/**
	 * Checks if a resource is registered (either static or dynamic).
	 *
	 * @param path The resource path to check
	 * @return true if the resource is registered
	 */
	public boolean isRegistered(ResourcePath path) {
		String key = path.toString();
		return staticResources.containsKey(key) || dynamicResources.containsKey(key);
	}

	/**
	 * Returns the count of registered resources.
	 *
	 * @return The total number of registered resources
	 */
	public int resourceCount() {
		return staticResources.size() + dynamicResources.size();
	}

	// ========== ResourceProvider Implementation ==========

	@Override
	public Set<String> getNamespaces() {
		return Collections.unmodifiableSet(namespaces);
	}

	@Override
	public Stream<ResourcePath> list(ResourcePath path, boolean recursive, ResourceFilter filter) {
		String namespace = path.namespace();
		String directory = path.directory();

		Stream<String> allKeys = Stream.concat(
				staticResources.keySet().stream(),
				dynamicResources.keySet().stream()
		).distinct();

		return allKeys
				.map(ResourcePath::parse)
				.filter(rp -> rp.namespace().equals(namespace))
				.filter(rp -> matchesDirectory(rp, directory, recursive))
				.filter(filter);
	}

	@Override
	public Stream<OpenIdentifier> list(OpenIdentifier path, boolean recursive) {
		return list(ResourcePath.directory(path.namespace(), path.path()), recursive, ResourceFilter.JSON)
				.map(ResourcePath::toIdentifier);
	}

	/**
	 * Checks if a resource path matches the search directory.
	 */
	private boolean matchesDirectory(ResourcePath resourcePath, String searchDir, boolean recursive) {
		String resourceDir = resourcePath.directory();

		if (searchDir.isEmpty()) {
			// Root directory: match everything (recursive) or only root-level files (non-recursive)
			return recursive || !resourceDir.contains("/");
		}

		if (recursive) {
			// Match files in the directory or any subdirectory
			return resourceDir.equals(searchDir) || resourceDir.startsWith(searchDir + "/");
		} else {
			// Match only files directly in the directory
			return resourceDir.equals(searchDir);
		}
	}

	@Override
	public Optional<InputStream> read(ResourcePath path) {
		String key = path.toString();

		// Check static resources first
		byte[] staticContent = staticResources.get(key);
		if (staticContent != null) {
			return Optional.of(new ByteArrayInputStream(staticContent));
		}

		// Check dynamic resources
		Supplier<byte[]> dynamicSupplier = dynamicResources.get(key);
		if (dynamicSupplier != null) {
			try {
				byte[] content = dynamicSupplier.get();
				if (content != null) {
					return Optional.of(new ByteArrayInputStream(content));
				}
			} catch (Exception e) {
				LOGGER.error("Error generating dynamic resource {}: {}", path, e.getMessage());
			}
		}

		return Optional.empty();
	}

	@Override
	public Optional<InputStream> read(OpenIdentifier identifier) {
		return read(ResourcePath.fromIdentifier(identifier));
	}

	@Override
	public boolean exists(ResourcePath path) {
		return isRegistered(path);
	}

	@Override
	public int priority() {
		return providerPriority;
	}

	@Override
	public String name() {
		return "Programmatic[" + providerName + "]";
	}
}
