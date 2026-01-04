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
import java.net.URI;
import java.net.URL;
import java.nio.file.*;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * A ResourceProvider for classpath and filesystem resources.
 * <p>
 * Supports two modes:
 * <ul>
 *   <li>Classpath mode: Loads resources from JARs and classpath entries</li>
 *   <li>Filesystem mode: Loads resources from a local directory (for development)</li>
 * </ul>
 * <p>
 * For new code, consider using {@link FileSystemResourceProvider} for filesystem access
 * and this class only for classpath/JAR access.
 */
public class ClassPathResourceProvider implements ResourceProvider {
	private static final Logger LOGGER = LoggerFactory.getLogger(ClassPathResourceProvider.class);

	private final Path rootPath;
	private final String topLevelDirectory;
	private final ClassLoader classLoader;
	private final int providerPriority;
	private final Set<String> namespaces;

	/**
	 * Constructor for Classpath-based loading with full configuration.
	 *
	 * @param topLevelDirectory The root directory within the classpath (e.g., "data").
	 * @param namespaces        The namespaces this provider serves.
	 * @param priority          The provider priority for composite ordering.
	 */
	public ClassPathResourceProvider(String topLevelDirectory, Set<String> namespaces, int priority) {
		this.topLevelDirectory = topLevelDirectory.replaceAll("^/|/$", "");
		this.classLoader = Thread.currentThread().getContextClassLoader();
		this.rootPath = null;
		this.namespaces = Set.copyOf(namespaces);
		this.providerPriority = priority;
	}

	/**
	 * Constructor for Classpath-based loading with default settings.
	 *
	 * @param topLevelDirectory The root directory within the classpath (e.g., "data").
	 */
	public ClassPathResourceProvider(String topLevelDirectory) {
		this(topLevelDirectory, DEFAULT_NAMESPACES, PRIORITY_BASE);
	}

	/**
	 * Constructor for FileSystem-based loading with full configuration.
	 *
	 * @param rootPath   The absolute path to the root directory to scan.
	 * @param namespaces The namespaces this provider serves.
	 * @param priority   The provider priority for composite ordering.
	 */
	public ClassPathResourceProvider(Path rootPath, Set<String> namespaces, int priority) {
		this.rootPath = rootPath;
		this.topLevelDirectory = "";
		this.classLoader = null;
		this.namespaces = Set.copyOf(namespaces);
		this.providerPriority = priority;
	}

	/**
	 * Constructor for FileSystem-based loading with default settings.
	 *
	 * @param rootPath The absolute path to the root directory to scan.
	 */
	public ClassPathResourceProvider(Path rootPath) {
		this(rootPath, DEFAULT_NAMESPACES, PRIORITY_BASE);
	}

	@Override
	public Set<String> getNamespaces() {
		return namespaces;
	}

	@Override
	public Stream<ResourcePath> list(ResourcePath path, boolean recursive, ResourceFilter filter) {
		if (this.rootPath != null) {
			return listFromFileSystemRoot(path, recursive, filter);
		} else {
			return listFromClasspath(path, recursive, filter);
		}
	}

	@Override
	public Stream<OpenIdentifier> list(OpenIdentifier path, boolean recursive) {
		return list(ResourcePath.directory(path.namespace(), path.path()), recursive, ResourceFilter.JSON)
				.map(ResourcePath::toIdentifier);
	}

	@Override
	public int priority() {
		return providerPriority;
	}

	@Override
	public String name() {
		if (rootPath != null) {
			return "ClassPath[FileSystem:" + rootPath + "]";
		}
		return "ClassPath[" + topLevelDirectory + "]";
	}

	@Override
	public Optional<InputStream> read(ResourcePath path) {
		String relativePath = topLevelDirectory.isEmpty() ?
				path.namespace() + "/" + path.fullPath() :
				topLevelDirectory + "/" + path.namespace() + "/" + path.fullPath();

		if (this.rootPath != null) {
			Path resourcePath = this.rootPath.resolve(relativePath);
			try {
				return Optional.of(Files.newInputStream(resourcePath));
			} catch (IOException e) {
				LOGGER.trace("Resource not found or could not be read from file system path: {}", resourcePath);
				return Optional.empty();
			}
		} else {
			InputStream stream = classLoader.getResourceAsStream(relativePath);
			if (stream == null) {
				LOGGER.trace("Resource not found on classpath: {}", relativePath);
			}
			return Optional.ofNullable(stream);
		}
	}

	@Override
	public Optional<InputStream> read(OpenIdentifier identifier) {
		return read(ResourcePath.fromIdentifier(identifier));
	}

	private Path getSearchRoot(ResourcePath path) {
		Path searchRoot = this.rootPath;
		if (!topLevelDirectory.isEmpty()) {
			searchRoot = searchRoot.resolve(topLevelDirectory);
		}
		return searchRoot.resolve(path.namespace());
	}

	private Stream<ResourcePath> listFromFileSystemRoot(ResourcePath path, boolean recursive, ResourceFilter filter) {
		try {
			Path searchRoot = getSearchRoot(path);
			URL rootUrl = searchRoot.toUri().toURL();
			return listResourcesFromUrl(rootUrl, path.directory(), path.namespace(), recursive, filter);
		} catch (Exception e) {
			LOGGER.error("Failed to list resources from file system path: {}", rootPath, e);
			return Stream.empty();
		}
	}

	private Stream<ResourcePath> listFromClasspath(ResourcePath path, boolean recursive, ResourceFilter filter) {
		// Build full path including target directory since getResources() doesn't work reliably for directories
		String fullPath = (topLevelDirectory.isEmpty() ? "" : topLevelDirectory + "/") +
						  path.namespace() + "/" +
						  (path.directory().isEmpty() ? "" : path.directory() + "/");
		try {
			Enumeration<URL> urls = classLoader.getResources(fullPath);
			if (urls == null || !urls.hasMoreElements()) {
				// Fallback: try just the namespace directory
				String baseClasspathPath = (topLevelDirectory.isEmpty() ? "" : topLevelDirectory + "/") + path.namespace() + "/";
				urls = classLoader.getResources(baseClasspathPath);
				if (urls == null || !urls.hasMoreElements()) {
					return Stream.empty();
				}
				// Use the original approach with targetDirectory
				return Collections.list(urls).stream()
						.flatMap(url -> {
							try {
								return listResourcesFromUrl(url, path.directory(), path.namespace(), recursive, filter);
							} catch (Exception e) {
								LOGGER.error("Failed to list resources from URL: {}", url, e);
								return Stream.empty();
							}
						});
			}

			// Direct path approach - the URL points to the target directory, but we still need to pass the target path
			return Collections.list(urls).stream()
					.flatMap(url -> {
						try {
							return listResourcesFromUrl(url, path.directory(), path.namespace(), recursive, filter);
						} catch (Exception e) {
							LOGGER.error("Failed to list resources from URL: {}", url, e);
							return Stream.empty();
						}
					});
		} catch (IOException e) {
			LOGGER.error("Error listing resources from classpath path: {}", fullPath, e);
			return Stream.empty();
		}
	}

	private Stream<ResourcePath> listResourcesFromUrl(URL namespaceRootUrl, String targetDirectory, String namespace, boolean recursive, ResourceFilter filter) throws Exception {
		URI uri = namespaceRootUrl.toURI();
		if ("jar".equals(uri.getScheme())) {
			return listResourcesFromJar(uri, targetDirectory, namespace, recursive, filter);
		} else {
			return listResourcesFromFileSystem(Paths.get(uri), targetDirectory, namespace, recursive, filter);
		}
	}

	private Stream<ResourcePath> listResourcesFromFileSystem(Path namespaceRootPath, String targetDirectory, String namespace, boolean recursive, ResourceFilter filter) throws IOException {
		// Check if namespaceRootPath already ends with targetDirectory (happens when URL already points to target)
		Path startPath;
		Path effectiveNamespaceRoot;
		if (!targetDirectory.isEmpty() && namespaceRootPath.endsWith(targetDirectory.replace('/', java.io.File.separatorChar))) {
			// The URL already points to the target directory, so use it directly
			startPath = namespaceRootPath;
			effectiveNamespaceRoot = namespaceRootPath.getParent();
		} else {
			startPath = namespaceRootPath.resolve(targetDirectory);
			effectiveNamespaceRoot = namespaceRootPath;
		}

		if (!Files.exists(startPath) || !Files.isDirectory(startPath)) {
			return Stream.empty();
		}

		int maxDepth = recursive ? Integer.MAX_VALUE : 1;
		try (Stream<Path> walk = Files.walk(startPath, maxDepth)) {
			return walk
					.filter(Files::isRegularFile)
					.map(filePath -> {
						// Relativize from effectiveNamespaceRoot to include the full path from namespace root
						Path relativePath = effectiveNamespaceRoot.relativize(filePath);
						String relativePathString = relativePath.toString().replace('\\', '/');
						return ResourcePath.fromIdentifier(new OpenIdentifier(namespace, relativePathString));
					})
					.filter(filter)
					.toList().stream();
		}
	}

	private Stream<ResourcePath> listResourcesFromJar(URI namespaceRootUri, String targetDirectory, String namespace, boolean recursive, ResourceFilter filter) throws Exception {
		FileSystem fs = getFileSystem(namespaceRootUri);
		String[] uriParts = namespaceRootUri.toString().split("!");
		if (uriParts.length < 2) {
			LOGGER.warn("Malformed JAR URI, cannot find internal path: {}", namespaceRootUri);
			return Stream.empty();
		}
		String internalPathStr = uriParts[1].startsWith("/") ? uriParts[1].substring(1) : uriParts[1];

		Path namespaceRootInJar = fs.getPath(internalPathStr);
		Path startPathInJar = namespaceRootInJar.resolve(targetDirectory);

		if (!Files.exists(startPathInJar) || !Files.isDirectory(startPathInJar)) {
			return Stream.empty();
		}

		int maxDepth = recursive ? Integer.MAX_VALUE : 1;
		try (Stream<Path> walk = Files.walk(startPathInJar, maxDepth)) {
			return walk
					.filter(Files::isRegularFile)
					.map(filePath -> {
						// Relativize from namespaceRootInJar to include the full path from namespace root
						Path relativePath = namespaceRootInJar.relativize(filePath);
						String relativePathString = relativePath.toString().replace('\\', '/');
						return ResourcePath.fromIdentifier(new OpenIdentifier(namespace, relativePathString));
					})
					.filter(filter)
					.toList().stream();
		}
	}

	private static FileSystem getFileSystem(URI uri) throws IOException {
		try {
			return FileSystems.getFileSystem(uri);
		} catch (FileSystemNotFoundException e) {
			return FileSystems.newFileSystem(uri, Collections.emptyMap());
		}
	}
}
