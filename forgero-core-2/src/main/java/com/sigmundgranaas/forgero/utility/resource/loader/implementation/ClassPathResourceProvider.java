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
import java.util.stream.Stream;

public class ClassPathResourceProvider implements ResourceProvider {
	private static final Logger LOGGER = LoggerFactory.getLogger(ClassPathResourceProvider.class);
	private String topLevelDirectory; // e.g., "assets"
	private final ClassLoader classLoader;

	public ClassPathResourceProvider(String topLevelDirectory) {
		this.topLevelDirectory = topLevelDirectory.startsWith("/") ? topLevelDirectory.substring(1) : topLevelDirectory;
		this.topLevelDirectory = this.topLevelDirectory.endsWith("/") ? this.topLevelDirectory.substring(0, this.topLevelDirectory.length() - 1) : this.topLevelDirectory;
		this.classLoader = Thread.currentThread().getContextClassLoader();
	}

	private String buildFullPathForRead(OpenIdentifier identifier) {
		return topLevelDirectory + "/" + identifier.namespace() + "/" + identifier.path();
	}

	@Override
	public Stream<OpenIdentifier> list(OpenIdentifier path, boolean recursive) {
		// e.g., "assets/forgero/"
		String baseClasspathPath = topLevelDirectory + "/" + path.namespace() + "/";
		try {
			// getResources (plural) finds ALL directories matching the path from ALL jars.
			Enumeration<URL> urls = classLoader.getResources(baseClasspathPath);
			if (urls == null || !urls.hasMoreElements()) {
				return Stream.empty();
			}

			return Collections.list(urls).stream()
					.flatMap(url -> {
						try {
							// For each URL found, list the resources inside the target sub-directory.
							return listResourcesFromUrl(url, path.path(), path.namespace(), recursive);
						} catch (Exception e) {
							LOGGER.error("Failed to list resources from URL: " + url, e);
							return Stream.empty(); // Continue with other URLs even if one fails
						}
					});
		} catch (IOException e) {
			LOGGER.error("Error listing resources from classpath path: " + baseClasspathPath, e);
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

	private Stream<OpenIdentifier> listResourcesFromFileSystem(Path namespaceRootPath, String targetDirectory, String namespace, boolean recursive) throws Exception {
		Path startPath = namespaceRootPath.resolve(targetDirectory);
		if (!Files.exists(startPath) || !Files.isDirectory(startPath)) {
			return Stream.empty();
		}

		int maxDepth = recursive ? Integer.MAX_VALUE : 1;
		try (Stream<Path> walk = Files.walk(startPath, maxDepth)) {
			return walk
					.filter(Files::isRegularFile)
					.map(filePath -> {
						Path relativePath = namespaceRootPath.relativize(filePath);
						String relativePathString = relativePath.toString().replace('\\', '/');
						return new OpenIdentifier(namespace, relativePathString);
					})
					.toList().stream(); // .toList() to eagerly close the file walker stream
		}
	}

	private Stream<OpenIdentifier> listResourcesFromJar(URI namespaceRootUri, String targetDirectory, String namespace, boolean recursive) throws Exception {
		FileSystem fs = getFileSystem(namespaceRootUri);

		// ** THE FIX IS HERE **
		// Use toString() and split at "!" to reliably get the path inside the JAR.
		// .getPath() returns null for jar: URIs, causing the NullPointerException.
		String[] uriParts = namespaceRootUri.toString().split("!");
		if (uriParts.length < 2) {
			LOGGER.warn("Malformed JAR URI, cannot find internal path: {}", namespaceRootUri);
			return Stream.empty();
		}
		String internalPathStr = uriParts[1];

		// The internal path often starts with a "/", which must be removed for fs.getPath()
		if (internalPathStr.startsWith("/")) {
			internalPathStr = internalPathStr.substring(1);
		}

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
						Path relativePath = namespaceRootInJar.relativize(filePath);
						String relativePathString = relativePath.toString().replace('\\', '/');
						return new OpenIdentifier(namespace, relativePathString);
					})
					.toList().stream(); // .toList() to eagerly close the file walker stream
		}
	}

	private static synchronized FileSystem getFileSystem(URI uri) throws IOException {
		String[] parts = uri.toString().split("!");
		URI jarUri = URI.create(parts[0]);
		try {
			return FileSystems.getFileSystem(jarUri);
		} catch (FileSystemNotFoundException e) {
			return FileSystems.newFileSystem(jarUri, Collections.emptyMap());
		}
	}

	@Override
	public Optional<InputStream> read(OpenIdentifier identifier) {
		String fullPath = buildFullPathForRead(identifier);
		InputStream stream = classLoader.getResourceAsStream(fullPath);
		if (stream == null) {
			LOGGER.trace("Resource not found with path: {}", fullPath);
		}
		return Optional.ofNullable(stream);
	}
}
