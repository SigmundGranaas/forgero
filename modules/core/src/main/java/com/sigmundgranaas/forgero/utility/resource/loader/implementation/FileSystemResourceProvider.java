package com.sigmundgranaas.forgero.utility.resource.loader.implementation;

import static com.sigmundgranaas.forgero.utility.resource.loader.api.ResourceConstants.*;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.utility.resource.loader.api.ResourceFilter;
import com.sigmundgranaas.forgero.utility.resource.loader.api.ResourcePath;
import com.sigmundgranaas.forgero.utility.resource.loader.api.ResourceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * A {@link ResourceProvider} for filesystem-based resources.
 * <p>
 * FileSystemResourceProvider provides access to resources stored on the local filesystem,
 * typically used during development for hot-reload capabilities or for user-provided
 * resource overrides.
 * <p>
 * Unlike {@link ClassPathResourceProvider}, this implementation directly accesses the
 * filesystem and does not cache file listings, making it suitable for hot-reload scenarios
 * where files may change during runtime.
 *
 * <h2>Directory Structure</h2>
 * <p>
 * The provider expects resources organized as:
 * <pre>
 * rootPath/
 *   topLevelDirectory/  (e.g., "data" or "assets")
 *     namespace/        (e.g., "forgero", "minecraft")
 *       directory/      (e.g., "materials", "shapes")
 *         file.ext
 * </pre>
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * Path devResources = Path.of("src/main/resources");
 *
 * FileSystemResourceProvider provider = new FileSystemResourceProvider(
 *     devResources,
 *     "data",
 *     Set.of("forgero", "minecraft"),
 *     50
 * );
 *
 * // List all JSON files in materials directory
 * Stream<ResourcePath> materials = provider.list(
 *     ResourcePath.directory("forgero", "materials"),
 *     true,
 *     ResourceFilter.JSON
 * );
 * }</pre>
 *
 * @see WatchingResourceProvider for file watching support
 */
public class FileSystemResourceProvider implements ResourceProvider {
	private static final Logger LOGGER = LoggerFactory.getLogger(FileSystemResourceProvider.class);

	private final Path rootPath;
	private final String topLevelDirectory;
	private final Set<String> namespaces;
	private final int providerPriority;

	/**
	 * Creates a new FileSystemResourceProvider.
	 *
	 * @param rootPath          The root path to the resources directory
	 * @param topLevelDirectory The top-level directory within the root (e.g., "data", "assets")
	 * @param namespaces        The set of namespaces this provider serves
	 * @param priority          The provider priority
	 */
	public FileSystemResourceProvider(Path rootPath, String topLevelDirectory, Set<String> namespaces, int priority) {
		this.rootPath = Objects.requireNonNull(rootPath, "rootPath cannot be null").toAbsolutePath().normalize();
		this.topLevelDirectory = normalizeTopLevelDirectory(topLevelDirectory);
		this.namespaces = Set.copyOf(Objects.requireNonNull(namespaces, "namespaces cannot be null"));
		this.providerPriority = priority;

		if (!Files.exists(this.rootPath)) {
			LOGGER.warn("FileSystemResourceProvider root path does not exist: {}", this.rootPath);
		}
	}

	/**
	 * Creates a new FileSystemResourceProvider with default namespaces and priority.
	 *
	 * @param rootPath          The root path to the resources directory
	 * @param topLevelDirectory The top-level directory within the root
	 */
	public FileSystemResourceProvider(Path rootPath, String topLevelDirectory) {
		this(rootPath, topLevelDirectory, DEFAULT_NAMESPACES, PRIORITY_BASE);
	}

	/**
	 * Creates a new FileSystemResourceProvider with no top-level directory.
	 *
	 * @param rootPath The root path (directly contains namespace directories)
	 */
	public FileSystemResourceProvider(Path rootPath) {
		this(rootPath, "", DEFAULT_NAMESPACES, PRIORITY_BASE);
	}

	@Override
	public Set<String> getNamespaces() {
		return namespaces;
	}

	@Override
	public Stream<ResourcePath> list(ResourcePath path, boolean recursive, ResourceFilter filter) {
		Path searchRoot = resolveSearchPath(path);

		if (!Files.exists(searchRoot) || !Files.isDirectory(searchRoot)) {
			LOGGER.trace("Search path does not exist or is not a directory: {}", searchRoot);
			return Stream.empty();
		}

		int maxDepth = recursive ? Integer.MAX_VALUE : 1;
		Path namespaceRoot = resolveNamespacePath(path.namespace());

		try {
			try (Stream<Path> walk = Files.walk(searchRoot, maxDepth)) {
				return walk
						.filter(Files::isRegularFile)
						.map(filePath -> toResourcePath(filePath, namespaceRoot, path.namespace()))
						.filter(filter)
						.toList().stream(); // Collect to avoid stream closed issues
			}
		} catch (IOException e) {
			LOGGER.error("Error listing resources from path: {}", searchRoot, e);
			return Stream.empty();
		}
	}

	@Override
	public Stream<OpenIdentifier> list(OpenIdentifier path, boolean recursive) {
		return list(ResourcePath.directory(path.namespace(), path.path()), recursive, ResourceFilter.JSON)
				.map(ResourcePath::toIdentifier);
	}

	@Override
	public Optional<InputStream> read(ResourcePath path) {
		Path fullPath = resolveFilePath(path);

		if (!Files.exists(fullPath)) {
			LOGGER.trace("Resource not found: {}", fullPath);
			return Optional.empty();
		}

		try {
			return Optional.of(Files.newInputStream(fullPath));
		} catch (IOException e) {
			LOGGER.error("Error reading resource: {}", fullPath, e);
			return Optional.empty();
		}
	}

	@Override
	public Optional<InputStream> read(OpenIdentifier identifier) {
		return read(ResourcePath.fromIdentifier(identifier));
	}

	@Override
	public boolean exists(ResourcePath path) {
		return Files.exists(resolveFilePath(path));
	}

	@Override
	public int priority() {
		return providerPriority;
	}

	@Override
	public String name() {
		return "FileSystem[" + rootPath + "]";
	}

	/**
	 * Returns the root path of this provider.
	 *
	 * @return The root path
	 */
	public Path getRootPath() {
		return rootPath;
	}

	/**
	 * Returns the top-level directory within the root.
	 *
	 * @return The top-level directory
	 */
	public String getTopLevelDirectory() {
		return topLevelDirectory;
	}

	// ========== Path Resolution Methods ==========

	/**
	 * Resolves the search path for listing resources.
	 */
	private Path resolveSearchPath(ResourcePath path) {
		Path base = topLevelDirectory.isEmpty() ? rootPath : rootPath.resolve(topLevelDirectory);
		base = base.resolve(path.namespace());
		return path.directory().isEmpty() ? base : base.resolve(path.directory());
	}

	/**
	 * Resolves the namespace root path.
	 */
	private Path resolveNamespacePath(String namespace) {
		Path base = topLevelDirectory.isEmpty() ? rootPath : rootPath.resolve(topLevelDirectory);
		return base.resolve(namespace);
	}

	/**
	 * Resolves the full path for reading a specific file.
	 */
	private Path resolveFilePath(ResourcePath path) {
		return resolveNamespacePath(path.namespace()).resolve(path.fullPath());
	}

	/**
	 * Converts a filesystem path to a ResourcePath.
	 */
	private ResourcePath toResourcePath(Path filePath, Path namespaceRoot, String namespace) {
		Path relativePath = namespaceRoot.relativize(filePath);
		String relativeStr = relativePath.toString().replace('\\', '/');
		return ResourcePath.fromIdentifier(new OpenIdentifier(namespace, relativeStr));
	}

	/**
	 * Normalizes the top-level directory by removing leading/trailing slashes.
	 */
	private static String normalizeTopLevelDirectory(String dir) {
		if (dir == null) {
			return "";
		}
		return dir.replaceAll("^/+|/+$", "");
	}
}
