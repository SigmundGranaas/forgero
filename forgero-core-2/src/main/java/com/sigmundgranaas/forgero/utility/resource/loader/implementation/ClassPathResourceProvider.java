package com.sigmundgranaas.forgero.utility.resource.loader.implementation;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
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

public class ClassPathResourceProvider implements ResourceProvider {
	private static final Logger LOGGER = LoggerFactory.getLogger(ClassPathResourceProvider.class);

	private final Path rootPath;
	private final String topLevelDirectory;
	private final ClassLoader classLoader;

	/**
	 * Constructor for Classpath-based loading.
	 *
	 * @param topLevelDirectory The root directory within the classpath (e.g., "/assets").
	 */
	public ClassPathResourceProvider(String topLevelDirectory) {
		this.topLevelDirectory = topLevelDirectory.replaceAll("^/|/$", "");
		this.classLoader = Thread.currentThread().getContextClassLoader();
		this.rootPath = null;
	}

	/**
	 * Constructor for FileSystem-based loading.
	 *
	 * @param rootPath The absolute path to the root directory to scan (e.g., ".../src/main/resources").
	 */
	public ClassPathResourceProvider(Path rootPath) {
		this.rootPath = rootPath;
		this.topLevelDirectory = ""; // The root path already points to the resource root, no need to add anything.
		this.classLoader = null;
	}

	@Override
	public Set<String> getNamespaces() {
		return Set.of("forgero", "minecraft");
	}

	@Override
	public Stream<OpenIdentifier> list(OpenIdentifier path, boolean recursive) {
		if (this.rootPath != null) {
			return listFromFileSystemRoot(path, recursive);
		} else {
			return listFromClasspath(path, recursive);
		}
	}

	@Override
	public Optional<InputStream> read(OpenIdentifier identifier) {
		String relativePath = topLevelDirectory.isEmpty() ?
				identifier.namespace() + "/" + identifier.path() :
				topLevelDirectory + "/" + identifier.namespace() + "/" + identifier.path();

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

	private Path getSearchRoot(OpenIdentifier path) {
		Path searchRoot = this.rootPath;
		if (!topLevelDirectory.isEmpty()) {
			searchRoot = searchRoot.resolve(topLevelDirectory);
		}
		return searchRoot.resolve(path.namespace());
	}

	private Stream<OpenIdentifier> listFromFileSystemRoot(OpenIdentifier path, boolean recursive) {
		try {
			Path searchRoot = getSearchRoot(path);
			URL rootUrl = searchRoot.toUri().toURL();
			return listResourcesFromUrl(rootUrl, path.path(), path.namespace(), recursive);
		} catch (Exception e) {
			LOGGER.error("Failed to list resources from file system path: {}", rootPath, e);
			return Stream.empty();
		}
	}

	private Stream<OpenIdentifier> listFromClasspath(OpenIdentifier path, boolean recursive) {
		String baseClasspathPath = (topLevelDirectory.isEmpty() ? "" : topLevelDirectory + "/") + path.namespace() + "/";
		try {
			Enumeration<URL> urls = classLoader.getResources(baseClasspathPath);
			if (urls == null || !urls.hasMoreElements()) {
				return Stream.empty();
			}

			return Collections.list(urls).stream()
					.flatMap(url -> {
						try {
							return listResourcesFromUrl(url, path.path(), path.namespace(), recursive);
						} catch (Exception e) {
							LOGGER.error("Failed to list resources from URL: {}", url, e);
							return Stream.empty();
						}
					});
		} catch (IOException e) {
			LOGGER.error("Error listing resources from classpath path: {}", baseClasspathPath, e);
			return Stream.empty();
		}
	}

	private Stream<OpenIdentifier> listResourcesFromUrl(URL namespaceRootUrl, String targetDirectory, String namespace, boolean recursive) throws Exception {
		URI uri = namespaceRootUrl.toURI();
		if ("jar".equals(uri.getScheme())) {
			return listResourcesFromJar(uri, targetDirectory, namespace, recursive);
		} else {
			return listResourcesFromFileSystem(Paths.get(uri), targetDirectory, namespace, recursive);
		}
	}

	private Stream<OpenIdentifier> listResourcesFromFileSystem(Path namespaceRootPath, String targetDirectory, String namespace, boolean recursive) throws IOException {
		Path startPath = namespaceRootPath.resolve(targetDirectory);
		if (!Files.exists(startPath) || !Files.isDirectory(startPath)) {
			return Stream.empty();
		}

		int maxDepth = recursive ? Integer.MAX_VALUE : 1;
		try (Stream<Path> walk = Files.walk(startPath, maxDepth)) {
			return walk
					.filter(Files::isRegularFile)
					.filter(p -> p.toString().endsWith(".json"))
					.map(filePath -> {
						Path relativePath = namespaceRootPath.relativize(filePath);
						String relativePathString = relativePath.toString().replace('\\', '/');
						return new OpenIdentifier(namespace, relativePathString);
					})
					.toList().stream();
		}
	}

	private Stream<OpenIdentifier> listResourcesFromJar(URI namespaceRootUri, String targetDirectory, String namespace, boolean recursive) throws Exception {
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
					.filter(p -> p.toString().endsWith(".json"))
					.map(filePath -> {
						Path relativePath = namespaceRootInJar.relativize(filePath);
						String relativePathString = relativePath.toString().replace('\\', '/');
						return new OpenIdentifier(namespace, relativePathString);
					})
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
